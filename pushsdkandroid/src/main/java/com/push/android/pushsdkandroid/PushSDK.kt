package com.push.android.pushsdkandroid

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.push.android.pushsdkandroid.core.APIHandler
import com.push.android.pushsdkandroid.core.PushSdkSavedDataProvider
import com.push.android.pushsdkandroid.models.*
import com.push.android.pushsdkandroid.models.PushServerApiResponse
import com.push.android.pushsdkandroid.utils.Info
import com.push.android.pushsdkandroid.utils.PushSDKLogger
import kotlin.properties.Delegates

/**
 * Main class, used for initialization. Only works with API v3.0
 * @see PushServerAnswerGeneral
 * @param context the context you would like to use
 * @param baseApiUrl base api url, like "https://example.io/api/3.0/"
 * @param log_level (optional) logging level
 */
@Suppress("unused")
class PushSDK(
    context: Context,
    baseApiUrl: String,
    log_level: LogLevels = LogLevels.PUSHSDK_LOG_LEVEL_ERROR,
    enableAutoDeliveryReport : Boolean = true
) {

    /**
     * Constants and public methods
     */
    companion object {


        /**
         * Get SDK version
         * @return SDK version name
         */
        fun getSDKVersionName(): String {
            return BuildConfig.VERSION_NAME
        }

        /**
         * Intent action when user clicks a notification
         */
        const val NOTIFICATION_CLICK_INTENT_ACTION = "pushsdk.intent.action.notification"

        /**
         * Name of the extra inside the intent that broadcasts push data
         */
        const val NOTIFICATION_CLICK_PUSH_DATA_EXTRA_NAME = "data"

        /**
         * Name of the extra inside the intent that broadcasts bubble push data
         */
        const val NOTIFICATION_BUBBLES_PUSH_DATA_EXTRA_NAME = "data"

    }

    /**
     * Log levels
     */
    enum class LogLevels {
        PUSHSDK_LOG_LEVEL_ERROR,
        PUSHSDK_LOG_LEVEL_DEBUG
    }

    //init stuff
    private var context: Context by Delegates.notNull()
    private val pushSdkSavedDataProvider = PushSdkSavedDataProvider(context.applicationContext)
    private var apiHandler: APIHandler = APIHandler(context)
    private var pushDeviceType: String = ""


    //main class initialization
    init {
        this.context = context
        pushSdkSavedDataProvider.baseApiUrl = baseApiUrl
        pushSdkSavedDataProvider.logLevel = log_level.name
        pushSdkSavedDataProvider.enableAutoDeliveryReport = enableAutoDeliveryReport
        pushDeviceType = Info.getPhoneType(context)
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                updateRegistration()
            }
        } catch (e: Exception) {
            PushSDKLogger.error(
                "PushSDK.init registration update problem:\n${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
        }
    }

    /**
     * Update FCM token
     */
    private fun updateFCMToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    PushSDKLogger.debug(
                        context,
                        "Fetching FCM registration token failed: ${task.exception}"
                    )
                } else {
                    // Get new FCM registration token
                    val fcmToken = task.result

                    if (fcmToken.isNotEmpty() && !fcmToken.equals(pushSdkSavedDataProvider.firebaseRegistrationToken)) {
                        PushSDKLogger.debug(
                            context,
                            "Old FCM token: ${pushSdkSavedDataProvider.firebaseRegistrationToken}"
                        )
                        PushSDKLogger.debug(context, "New FCM token: $fcmToken")
                        pushSdkSavedDataProvider.firebaseRegistrationToken = fcmToken
                        PushSDKLogger.debug(context, "FCM token updated successfully.")
                    } else {
                        PushSDKLogger.debug(context, "New FCM token is either empty or up-to-date")
                        PushSDKLogger.debug(
                            context,
                            "Current FCM token: ${pushSdkSavedDataProvider.firebaseRegistrationToken}"
                        )
                        PushSDKLogger.debug(context, "Received FCM token: $fcmToken")
                    }
                }
            })
        } catch (e: Exception) {
            PushSDKLogger.error(e.stackTraceToString())
        }
    }

    /**
     * Clear local data
     */
    private fun clearData() {
        PushSDKLogger.debug(context, "Attempting to clear local registration data")
        pushSdkSavedDataProvider.registrationStatus = false
        pushSdkSavedDataProvider.deviceId = ""
        pushSdkSavedDataProvider.pushServerUserId = ""
        pushSdkSavedDataProvider.userMsisdn = ""
        pushSdkSavedDataProvider.pushServiceRegistrationDate = ""
    }

    /**
     * Save various params locally
     */
    private fun saveDataLocally(
        deviceId: String,
        push_k_user_msisdn: String,
        push_k_user_Password: String,
        push_k_registration_token: String,
        push_k_user_id: String,
        push_k_registration_createdAt: String,
        registrationStatus: Boolean
    ) {
        PushSDKLogger.debug(context, "Saving params locally")
        pushSdkSavedDataProvider.deviceId = deviceId
        pushSdkSavedDataProvider.userMsisdn = push_k_user_msisdn
        pushSdkSavedDataProvider.userPassword = push_k_user_Password
        pushSdkSavedDataProvider.pushServiceRegistrationDate = push_k_registration_createdAt
        pushSdkSavedDataProvider.pushServerUserId = push_k_user_id
        pushSdkSavedDataProvider.pushServiceRegistrationToken = push_k_registration_token
        pushSdkSavedDataProvider.registrationStatus = registrationStatus
    }

    //answer codes
    //200 - OK

    //answers from remote server
    //401 HTTP code – Dead token | (Client error) authentication error, probably errors
    //400 HTTP code – (Client error) request validation error, probably errors
    //500 HTTP code – (Server error) 

    //sdk errors
    //700 - internal SDK error
    //701 - already exists
    //704 - not registered (locally)
    //710 - unknown error

    //network errors
    //901 - failed registration with firebase

    //{
    //    "result":"Ok",
    //    "description":"",
    //    "code":200,
    //    "body":"{}"
    //}


    /**
     * Register the device
     * @param clientAPIKey API key that you would be provided with
     * @param appFingerprint APP fingerprint that you would be provided with
     * @param userMsisdn Device phone number
     * @param userPassword password, associated with device phone number
     * @param firebaseToken (Optional) your firebase cloud messaging token
     */
    fun registerNewDevice(
        clientAPIKey: String,
        appFingerprint: String,
        userMsisdn: String,
        userPassword: String,
        firebaseToken: String = ""
    ): PushServerAnswerRegister {
        PushSDKLogger.debug(
            context, "calling registerNewDevice with params:\n" +
                    "clientAPIKey $clientAPIKey\n" +
                    "appFingerprint $appFingerprint\n" +
                    "userMsisdn $userMsisdn\n" +
                    "userPassword $userPassword" +
                    "firebaseToken $firebaseToken"
        )
        var response: PushServerAnswerRegister
        try {
            updateFCMToken()
            val firebaseTokenToUse = if (firebaseToken == "") {
                pushSdkSavedDataProvider.firebaseRegistrationToken
            } else {
                firebaseToken
            }

            if (pushSdkSavedDataProvider.registrationStatus) {
                response = PushServerAnswerRegister(
                    code = 701,
                    deviceId = pushSdkSavedDataProvider.deviceId,
                    token = pushSdkSavedDataProvider.pushServiceRegistrationToken,
                    userId = pushSdkSavedDataProvider.pushServerUserId,
                    userPhone = pushSdkSavedDataProvider.userMsisdn,
                    createdAt = pushSdkSavedDataProvider.pushServiceRegistrationDate,
                    result = PushSDKAnswerResult.EXISTS,
                    description = "Device already registered."
                )
                PushSDKLogger.debug(context, "registerNewDevice response: $response")
                return response
            } else {
                if (firebaseTokenToUse.trim() == "") {
                    response = PushServerAnswerRegister(
                        code = 901,
                        description = "X_Push_Session_Id is empty. Maybe firebase registration problem",
                        result = PushSDKAnswerResult.FAILED,
                        deviceId = "unknown",
                        token = "unknown",
                        userId = "unknown",
                        userPhone = "unknown",
                        createdAt = "unknown"
                    )
                    PushSDKLogger.debug(context, "registerNewDevice response: $response")
                    return response
                } else {
                    val requestResponse = apiHandler.registerDevice(
                        clientAPIKey,
                        firebaseTokenToUse,
                        appFingerprint,
                        getSDKVersionName(),
                        userPassword,
                        userMsisdn,
                        pushDeviceType
                    )

                    when (requestResponse.code) {
                        200 -> {
                            val parent =
                                Gson().fromJson(requestResponse.body, JsonObject::class.java)
                            val deviceId = parent.getAsJsonObject("device").get("deviceId").asString
                            val token = parent.getAsJsonObject("session").get("token").asString
                            val userId = parent.getAsJsonObject("profile").get("userId").asString
                            val userPhone =
                                parent.getAsJsonObject("profile").get("userPhone").asString
                            val createdAt =
                                parent.getAsJsonObject("profile").get("createdAt").asString

                            saveDataLocally(
                                deviceId,
                                userMsisdn,
                                userPassword,
                                token,
                                userId,
                                createdAt,
                                true
                            )

                            response = PushServerAnswerRegister(
                                code = 200,
                                description = "Success",
                                result = PushSDKAnswerResult.OK,
                                deviceId = deviceId,
                                token = token,
                                userId = userId,
                                userPhone = userPhone,
                                createdAt = createdAt
                            )
                        }
                        401 -> {
                            response = PushServerAnswerRegister(
                                code = 401,
                                description = "(Client error) authentication error, probably errors",
                                result = PushSDKAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                            )
                        }
                        400 -> {
                            response = PushServerAnswerRegister(
                                code = 400,
                                description = "(Client error) request validation error",
                                result = PushSDKAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                            )
                        }
                        500 -> {
                            response = PushServerAnswerRegister(
                                code = 500,
                                description = "Server error",
                                result = PushSDKAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                            )
                        }
                        else -> {
                            response = PushServerAnswerRegister(
                                code = 710,
                                description = "Unknown error",
                                result = PushSDKAnswerResult.FAILED,
                                deviceId = "unknown",
                                token = "unknown",
                                userId = "unknown",
                                userPhone = "unknown",
                                createdAt = "unknown"
                            )
                        }
                    }
                }
            }
            PushSDKLogger.debug(context, "registerNewDevice response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(e.toString())
            return PushServerAnswerRegister(
                code = 700,
                description = "Internal SDK error",
                result = PushSDKAnswerResult.FAILED,
                deviceId = "unknown",
                token = "unknown",
                userId = "unknown",
                userPhone = "unknown",
                createdAt = "unknown"
            )

        }
    }

    /**
     * Unregister the current device from database (if registered)
     */
    fun unregisterCurrentDevice(): PushServerAnswerGeneral {
        PushSDKLogger.debug(context, "calling unregisterCurrentDevice")
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            val xPushSessionId = pushSdkSavedDataProvider.firebaseRegistrationToken
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.unregisterDevice(
                    "[\"${pushSdkSavedDataProvider.deviceId}\"]",
                    xPushSessionId,
                    pushSdkSavedDataProvider.pushServiceRegistrationToken
                )
                val deviceId = pushSdkSavedDataProvider.deviceId
                when (requestResponse.code) {
                    200 -> {
                        clearData()
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.OK,
                            "Success",
                            "{\"device\":\"$deviceId\"}"
                        )
                    }
                    401 -> {
                        clearData()
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Auth token is probably dead. Try to register your device again.",
                            requestResponse.body
                        )
                    }
                    else -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Error",
                            "{\"device\":\"$deviceId\"}"
                        )
                    }
                }
            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "unregisterCurrentDevice response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "unregisterCurrentDevice failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Unregister all devices registered with the current phone number from database
     *
     * @return PushKFunAnswerGeneral
     */
    fun unregisterAllDevices(): PushServerAnswerGeneral {
        PushSDKLogger.debug(context, "calling unregisterAllDevices")
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.getDeviceAll(
                    pushSdkSavedDataProvider.firebaseRegistrationToken,
                    pushSdkSavedDataProvider.pushServiceRegistrationToken
                )
                if (requestResponse.code == 200) {
                    val devices = Gson().fromJson(requestResponse.body, JsonObject::class.java)
                        .getAsJsonArray("devices")
                    val deviceIds = JsonArray()
                    for (device in devices) {
                        deviceIds.add(device.asJsonObject.getAsJsonPrimitive("id").asString)
                    }
                    PushSDKLogger.debug(context, "generated deviceIds: $deviceIds")
                    val revokeRequestResponse: PushServerApiResponse = apiHandler.unregisterDevice(
                        deviceIds.toString(),
                        pushSdkSavedDataProvider.firebaseRegistrationToken, //_xPushSessionId
                        pushSdkSavedDataProvider.pushServiceRegistrationToken
                    )
                    when (revokeRequestResponse.code) {
                        200 -> {
                            clearData()
                            response = PushServerAnswerGeneral(
                                revokeRequestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                "{\"devices\":\"$deviceIds\"}"
                            )
                        }
                        401 -> {
                            clearData()
                            response = PushServerAnswerGeneral(
                                revokeRequestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                revokeRequestResponse.body
                            )
                        }
                        else -> {
                            response = PushServerAnswerGeneral(
                                revokeRequestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                "{\"devices\":\"$deviceIds\"}"
                            )
                        }
                    }
                } else {
                    response = PushServerAnswerGeneral(
                        710,
                        PushSDKAnswerResult.FAILED,
                        "Unknown error",
                        "unknown"
                    )
                }
            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "unregisterAllDevices response: $response")
            return response

        } catch (e: Exception) {
            PushSDKLogger.error(
                "unregisterAllDevices failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Get message history
     * @param periodInSeconds amount of time to get message history for
     *
     * @return PushKFunAnswerGeneral
     */
    fun getMessageHistory(periodInSeconds: Int): PushServerAnswerGeneral {
        PushSDKLogger.debug(
            context, "calling getMessageHistory with params:\n" +
                    "periodInSeconds $periodInSeconds"
        )
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.getHistory(
                    pushSdkSavedDataProvider.firebaseRegistrationToken, //_xPushSessionId
                    pushSdkSavedDataProvider.pushServiceRegistrationToken,
                    periodInSeconds
                )
                when (requestResponse.code) {
                    200 -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.OK,
                            "Success",
                            requestResponse.body
                        )
                    }
                    401 -> {
                        clearData()
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Auth token is probably dead. Try to register your device again.",
                            requestResponse.body
                        )
                    }
                    else -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Error",
                            requestResponse.body
                        )
                    }
                }

            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "getMessageHistory response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "getMessageHistory failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Get a list of all devices registered with the current phone number
     *
     * @return PushKFunAnswerGeneral
     */
    fun getAllRegisteredDevices(): PushServerAnswerGeneral {
        PushSDKLogger.debug(context, "calling getAllRegisteredDevices")
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.getDeviceAll(
                    pushSdkSavedDataProvider.firebaseRegistrationToken, //_xPushSessionId
                    pushSdkSavedDataProvider.pushServiceRegistrationToken
                )
                when (requestResponse.code) {
                    200 -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.OK,
                            "Success",
                            requestResponse.body
                        )
                    }
                    401 -> {
                        clearData()
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Auth token is probably dead. Try to register your device again.",
                            requestResponse.body
                        )
                    }
                    else -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Error",
                            requestResponse.body
                        )
                    }
                }

            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "getAllRegisteredDevices response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "getAllRegisteredDevices failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Update registration
     *
     * @return PushKFunAnswerGeneral
     */
    fun updateRegistration(): PushServerAnswerGeneral {
        PushSDKLogger.debug(context, "calling updateRegistration")
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.updateRegistration(
                    pushSdkSavedDataProvider.pushServiceRegistrationToken, //pushSdkSavedDataProvider.push_k_registration_token
                    pushSdkSavedDataProvider.firebaseRegistrationToken, // pushSdkSavedDataProvider.firebase_registration_token
                    getSDKVersionName(),
                    pushSdkSavedDataProvider.firebaseRegistrationToken,
                    pushDeviceType
                )
                when (requestResponse.code) {
                    200 -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.OK,
                            "Success",
                            requestResponse.body
                        )
                    }
                    401 -> {
                        clearData()
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Auth token is probably dead. Try to register your device again.",
                            requestResponse.body
                        )
                    }
                    else -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Error",
                            requestResponse.body
                        )
                    }
                }
            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "updateRegistration response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "updateRegistration failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Send a message to the server and receive a callback
     * @param messageId id of the message
     * @param messageText text of the message
     *
     * @return PushKFunAnswerGeneral
     */
    fun sendMessageCallback(
        messageId: String,
        messageText: String
    ): PushServerAnswerGeneral {
        PushSDKLogger.debug(
            context, "calling sendMessageCallback with params:\n" +
                    "messageId $messageId\n" +
                    "messageText $messageText"
        )
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                val requestResponse = apiHandler.messageCallback(
                    messageId,
                    messageText,
                    pushSdkSavedDataProvider.firebaseRegistrationToken, //_xPushSessionId
                    pushSdkSavedDataProvider.pushServiceRegistrationToken
                )
                when (requestResponse.code) {
                    200 -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.OK,
                            "Success",
                            requestResponse.body
                        )
                    }
                    401 -> {
                        clearData()
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Auth token is probably dead. Try to register your device again.",
                            requestResponse.body
                        )
                    }
                    else -> {
                        response = PushServerAnswerGeneral(
                            requestResponse.code,
                            PushSDKAnswerResult.FAILED,
                            "Error",
                            requestResponse.body
                        )
                    }
                }

            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "sendMessageCallback response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "sendMessageCallback failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Send delivery report for a specific message
     * @param messageId message id to send the delivery report for
     *
     * @return PushKFunAnswerGeneral
     */
    fun sendMessageDeliveryReport(messageId: String): PushServerAnswerGeneral {
        PushSDKLogger.debug(
            context, "calling sendMessageDeliveryReport with params:\n" +
                    "messageId $messageId"
        )
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                if (pushSdkSavedDataProvider.pushServiceRegistrationToken != ""
                    && pushSdkSavedDataProvider.firebaseRegistrationToken != ""
                ) {
                    val requestResponse = apiHandler.hMessageDr(
                        messageId,
                        pushSdkSavedDataProvider.firebaseRegistrationToken, //_xPushSessionId
                        pushSdkSavedDataProvider.pushServiceRegistrationToken
                    )
                    when (requestResponse.code) {
                        200 -> {
                            response = PushServerAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                requestResponse.body
                            )
                        }
                        401 -> {
                            clearData()
                            response = PushServerAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body
                            )
                        }
                        else -> {
                            response = PushServerAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                requestResponse.body
                            )
                        }
                    }


                } else {
                    response = PushServerAnswerGeneral(
                        700,
                        PushSDKAnswerResult.FAILED,
                        "Failed. firebase_registration_token or push_registration_token empty",
                        "{}"
                    )
                }

            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "sendMessageDeliveryReport response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "sendMessageDeliveryReport failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }


    /**
     * Checks undelivered message queue;
     *
     * @return PushKFunAnswerGeneral
     */
    fun checkMessageQueue(): PushServerAnswerGeneral {
        PushSDKLogger.debug(context, "calling checkMessageQueue")
        var response: PushServerAnswerGeneral
        try {
            updateFCMToken()
            if (pushSdkSavedDataProvider.registrationStatus) {
                if (pushSdkSavedDataProvider.firebaseRegistrationToken != ""
                    && pushSdkSavedDataProvider.pushServiceRegistrationToken != ""
                ) {
                    val requestResponse = apiHandler.getDevicePushMsgQueue(
                        pushSdkSavedDataProvider.firebaseRegistrationToken,
                        pushSdkSavedDataProvider.pushServiceRegistrationToken
                    )
                    when (requestResponse.code) {
                        200 -> {
                            response = PushServerAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.OK,
                                "Success",
                                requestResponse.body
                            )
                        }
                        401 -> {
                            clearData()
                            response = PushServerAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Auth token is probably dead. Try to register your device again.",
                                requestResponse.body
                            )
                        }
                        else -> {
                            response = PushServerAnswerGeneral(
                                requestResponse.code,
                                PushSDKAnswerResult.FAILED,
                                "Error",
                                requestResponse.body
                            )
                        }
                    }
                } else {
                    response = PushServerAnswerGeneral(
                        700,
                        PushSDKAnswerResult.FAILED,
                        "Failed. firebase_registration_token or push_k_registration_token empty",
                        "{}"
                    )
                }

            } else {
                response = PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
            PushSDKLogger.debug(context, "checkMessageQueue response: $response")
            return response
        } catch (e: Exception) {
            PushSDKLogger.error(
                "checkMessageQueue failed with exception: ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
            return PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }

    /**
     * Change phone number (Locally)
     * @param newMsisdn new phone number
     *
     * @return PushKFunAnswerGeneral
     */
    fun rewriteMsisdn(newMsisdn: String): PushServerAnswerGeneral {
        PushSDKLogger.debug(
            context, "calling rewriteMsisdn() with params:\n" +
                    "newMsisdn $newMsisdn"
        )
        return try {
            if (pushSdkSavedDataProvider.registrationStatus) {
                pushSdkSavedDataProvider.userMsisdn = newMsisdn
                PushServerAnswerGeneral(200, PushSDKAnswerResult.OK, "Success", "{}")
            } else {
                PushServerAnswerGeneral(
                    704,
                    PushSDKAnswerResult.FAILED,
                    "Registration data not found",
                    "Not registered"
                )
            }
        } catch (e: Exception) {
            PushSDKLogger.error("rewriteMsisdn() failed with exception: ${Log.getStackTraceString(e)}")
            PushServerAnswerGeneral(
                710,
                PushSDKAnswerResult.FAILED,
                "Unknown error",
                "unknown"
            )
        }
    }


    /**
     * Get user`s device data
     * @return UserDataModel object with fields
     * deviceOS - device OS
     * osVersion - version of device OS
     * deviceModel - model of device
     * deviceLanguage - interface language in this language
     * deviceLanguageEn - interface language in English
     * isoLanguageCode iso language code
     * iso3LanguageCode iso3 language code
     * timeZone - object of TimeZone
     * timeZoneShort - short time zone GMT, CET etc...
     * isoCountry - country iso code
     * iso3Country - country iso3 code
     * countryName - country name in device interface language
     * countryNameEn - country name in English
     */
    fun getUserData(): UserDataModel {
        return UserDataModel(
            deviceOS = Info.getOSType(),
            osVersion = Info.getAndroidVersion(),
            deviceModel = Info.getDeviceName(),
            deviceLanguage = Info.getLanguage(),
            deviceLanguageEn = Info.getLanguageInEn(),
            isoLanguageCode = Info.getLanguageISO(),
            iso3LanguageCode = Info.getLanguageISO3(),
            timeZone = Info.getDeviceTimeZone(),
            timeZoneShort = Info.getDeviceShortTimeZone(),
            isoCountry = Info.getCountryIsoCode(context),
            iso3Country = Info.getCountryIso3Code(context),
            countryName = Info.getCountryName(context),
            countryNameEn = Info.getCountryInEn(context)
        )
    }

    //TODO remove once confirmed useless
    /**
     * Change password (temporary - legacy method - useless)
     * @param newPassword new password
     *
     * @return PushKFunAnswerGeneral
     */
    fun rewritePassword(newPassword: String): PushServerAnswerGeneral {
        PushSDKLogger.debug(context, "rewrite_password start: $newPassword")
        return if (pushSdkSavedDataProvider.registrationStatus) {
            pushSdkSavedDataProvider.userPassword = newPassword
            PushServerAnswerGeneral(200, PushSDKAnswerResult.OK, "Success", "{}")
        } else {
            PushServerAnswerGeneral(
                704,
                PushSDKAnswerResult.FAILED,
                "Registration data not found",
                "Not registered"
            )
        }
    }
}
