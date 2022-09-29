package com.push.android.pushsdkandroid.core

import android.content.Context
import com.push.android.pushsdkandroid.utils.Info
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import com.push.android.pushsdkandroid.models.PushServerApiResponse
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

/**
 * Communication with push rest server (REST API)
 * Will return only server API's response codes
 */
internal class APIHandler(private val context: Context) {

    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)

    //parameters for procedures
    private val osVersion = Info.getAndroidVersion()
    private val deviceName = Info.getDeviceName()
    private val osType = Info.getOSType()

    private var headerClientApiKey = "X-Msghub-Client-API-Key"
    private var headerAppFingerprint = "X-Msghub-App-Fingerprint"
    private var headerSessionId = "X-Msghub-Session-Id"
    private var headerTimestamp = "X-Msghub-Timestamp"
    private var headerAuthToken = "X-Msghub-Auth-Token"

    //should start with slash
    private var deviceUpdatePath = "/device/update"
    private var deviceRegistrationPath = "/device/registration"
    private var deviceRevokePath = "/device/revoke"
    private var getDeviceAllPath = "/device/all"
    private var messageCallbackPath = "/message/callback"
    private var messageDeliveryReportPath = "/message/dr"
    private var messageQueuePath = "/message/queue"
    private var messageHistoryPath = "/message/history"

    /**
     * Enum of possible paths
     */
    enum class ApiPaths {
        DEVICE_UPDATE,
        DEVICE_REGISTRATION,
        DEVICE_REVOKE,
        GET_DEVICE_ALL,
        MESSAGE_CALLBACK,
        MESSAGE_DELIVERY_REPORT,
        MESSAGE_QUEUE,
        MESSAGE_HISTORY
    }

    //goes like "https://api.com/api" + "3.0" + "/device/update"
    /**
     * Get full URL path, e.g. https://example.io/api/2.3/message/dr
     * @param path which path to get full URL for
     */
    private fun getFullURLFor(path: ApiPaths): String {
        return URI(
            "${pushSdkSavedDataProvider.baseApiUrl}${
                when (path) {
                    ApiPaths.DEVICE_UPDATE -> deviceUpdatePath
                    ApiPaths.DEVICE_REGISTRATION -> deviceRegistrationPath
                    ApiPaths.DEVICE_REVOKE -> deviceRevokePath
                    ApiPaths.GET_DEVICE_ALL -> getDeviceAllPath
                    ApiPaths.MESSAGE_CALLBACK -> messageCallbackPath
                    ApiPaths.MESSAGE_DELIVERY_REPORT -> messageDeliveryReportPath
                    ApiPaths.MESSAGE_QUEUE -> messageQueuePath
                    ApiPaths.MESSAGE_HISTORY -> messageHistoryPath
                }
            }"
        ).normalize().toString()
    }

    enum class SupportedRestMethods {
        GET,
        POST
    }

    /**
     * Creates special token for use in requests
     */
    private fun hash(sss: String): String {
        return try {
            val bytes = sss.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val resp: String = digest.fold("") { str, it -> str + "%02x".format(it) }
            PushSDKLogger.debug(context, "hashing successful, input: $sss, output: $resp")
            resp
        } catch (e: Exception) {
            val output = "failed"
            PushSDKLogger.debug(context, "hashing error, input: $sss, output: $output")
            output
        }
    }

    /**
     * Make an API request
     *
     * @throws IllegalArgumentException when supplied with wrong URL protocol or wrong REST method
     */
    private fun makeRequest(
        headers: Map<String, String>,
        method: SupportedRestMethods,
        url: URL,
        postData: String = ""
    ): PushServerApiResponse {
        PushSDKLogger.debug(
            context, "Calling makeRequest with parameters:\n" +
                    "headers: $headers\n" +
                    "method: $method\n" +
                    "url: $url\n" +
                    "post data: $postData"
        )
        var requestResponseData = String()
        var requestResponseCode = 0

        val requestThread = Thread {
            try {
                val connection = when (url.protocol) {
                    "https" -> {
                        url.openConnection() as HttpsURLConnection
                    }
                    "http" -> {
                        url.openConnection() as HttpURLConnection
                    }
                    else -> {
                        PushSDKLogger.error("Unknown protocol used for api URL. The only supported protocols are: http, https")
                        throw IllegalArgumentException("Change your API URL protocol. The only supported protocols are: http, https")
                    }
                }
                connection.requestMethod = method.name

                //set headers
                connection.setRequestProperty("Content-Language", "en-US")
                connection.setRequestProperty("Content-Type", "application/json")
                for (header in headers) {
                    connection.setRequestProperty(header.key, header.value)
                }

                with(connection) {
                    requestMethod = method.name
                    when (method) {
                        SupportedRestMethods.GET -> {
                            requestResponseCode = responseCode
                            inputStream.bufferedReader().use {
                                requestResponseData = it.readLine().toString()

                                it.close()
                            }
                            PushSDKLogger.debug(
                                context,
                                "requestResponseCode: $requestResponseCode\n" +
                                        "requestResponseData: $requestResponseData"
                            )
                        }
                        SupportedRestMethods.POST -> {
                            doOutput = true
                            outputStream.use {
                                val postDataBytes = postData.toByteArray(Charset.forName("UTF-8"))
                                it.write(postDataBytes)
                                it.flush()
                                it.close()
                            }
                            requestResponseCode = responseCode
                            inputStream.bufferedReader().use {
                                val response = StringBuffer()
                                var inputLine = it.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = it.readLine()
                                }
                                requestResponseData = response.toString()

                                it.close()
                            }
                            PushSDKLogger.debug(
                                context,
                                "requestResponseCode: $requestResponseCode\n" +
                                        "requestResponseData: $requestResponseData"
                            )
                        }
                    }
                }


            } catch (e: Exception) {
                PushSDKLogger.error(e.stackTraceToString())
                requestResponseCode = 710
                requestResponseData = "unknown error"
            }
        }
        requestThread.start()
        requestThread.join()

        val answer = PushServerApiResponse(requestResponseCode, requestResponseData, 0)
        PushSDKLogger.debug(context, "Push server response: $answer")
        return answer
    }

    //GET requests

    /**
     * GET request to get message history
     */
    fun getHistory(
        sessionId: String,
        authToken: String,
        periodInSeconds: Int
    ): PushServerApiResponse {
        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.GET,
            URL("${getFullURLFor(ApiPaths.MESSAGE_HISTORY)}?startDate=${currentTimeSeconds - periodInSeconds}"),
            ""
        )
    }


    /**
     * GET request to get all registered devices
     */
    fun getDeviceAll(sessionId: String, authToken: String): PushServerApiResponse {
        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.GET,
            URL(getFullURLFor(ApiPaths.GET_DEVICE_ALL)),
            ""
        )
    }

    //POST requests

    /**
     * POST request to revoke registration
     */
    fun unregisterDevice(
        deviceList: String,
        sessionId: String,
        authToken: String
    ): PushServerApiResponse {
        val body = "{\"devices\":$deviceList}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.DEVICE_REVOKE)),
            body
        )
    }

    /**
     * POST request to update device registration
     */
    fun updateRegistration(
        authToken: String,
        sessionId: String,
        sdkVersion: String,
        fcmToken: String,
        deviceType: String
    ): PushServerApiResponse {
        val body =
            "{\"fcmToken\": \"$fcmToken\",\"osType\": \"$osType\",\"osVersion\": \"$osVersion\",\"deviceType\": \"$deviceType\",\"deviceName\": \"$deviceName\",\"sdkVersion\": \"$sdkVersion\" }"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.DEVICE_UPDATE)),
            body
        )
    }

    /**
     * Message callback - POST request
     */
    fun messageCallback(
        messageId: String,
        pushAnswer: String,
        sessionId: String,
        authToken: String
    ): PushServerApiResponse {
        val body = "{\"messageId\": \"$messageId\", \"answer\": \"$pushAnswer\"}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.MESSAGE_CALLBACK)),
            body
        )
    }

    /**
     * POST request - report message delivery
     *
     * @throws IllegalArgumentException when one of the arguments is empty
     */
    fun hMessageDr(
        messageId: String,
        sessionId: String,
        authToken: String
    ): PushServerApiResponse {
        val body = "{\"messageId\": \"$messageId\"}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.MESSAGE_DELIVERY_REPORT)),
            body
        )
    }

    /**
     * Obtain the push message queue;
     * Will also broadcast an intent with all the queued message
     */
    internal fun getDevicePushMsgQueue(
        sessionId: String,
        authToken: String
    ): PushServerApiResponse {
        val body = "{}"

        val currentTimeSeconds = System.currentTimeMillis() / 1000L
        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerTimestamp] = currentTimeSeconds.toString()
        headers[headerAuthToken] = hash("$authToken:$currentTimeSeconds")
        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.MESSAGE_QUEUE)),
            body
        )
    }

    /**
     * Registration POST request
     */
    fun registerDevice(
        apiKey: String,
        sessionId: String,
        appFingerprint: String,
        sdkVersion: String,
        userPassword: String,
        userPhone: String,
        deviceType: String
    ): PushServerApiResponse {
        val body =
            "{\"userPhone\":\"$userPhone\",\"userPass\":\"$userPassword\",\"osType\":\"$osType\",\"osVersion\":\"$osVersion\",\"deviceType\":\"$deviceType\",\"deviceName\":\"$deviceName\",\"sdkVersion\":\"$sdkVersion\"}"

        val headers = mutableMapOf<String, String>()
        headers[headerSessionId] = sessionId
        headers[headerClientApiKey] = apiKey
        headers[headerAppFingerprint] = appFingerprint

        return makeRequest(
            headers,
            SupportedRestMethods.POST,
            URL(getFullURLFor(ApiPaths.DEVICE_REGISTRATION)),
            body
        )
    }
}