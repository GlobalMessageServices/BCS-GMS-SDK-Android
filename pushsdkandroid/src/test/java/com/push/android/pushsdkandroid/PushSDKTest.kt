package com.push.android.pushsdkandroid

import androidx.test.platform.app.InstrumentationRegistry
import com.push.android.pushsdkandroid.core.APIHandler
import com.push.android.pushsdkandroid.utils.Info
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PushSDKTest {

    private lateinit var pushSDK: PushSDK
    private var context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var apiHandler: APIHandler

    @Before
    fun before() {
        pushSDK = PushSDK(context, "https://example.io/api/3.0/")
        apiHandler = APIHandler(context)
    }

    @Test
    @Throws(Exception::class)
    fun testRegistration() {
        val expectedCode1 = 901
        val expectedCode2 = 710
        val response1 = pushSDK.registerNewDevice(
            "test_api_key",
            "test_app_finger_print",
            "1234567890",
            "test_device"
        )
        val response2 = apiHandler.registerDevice(
            "test_api_key",
            "test_session_id",
            "test_app_finger_print",
            "test_sdk_version",
            "1",
            "1234567890",
            "test_device"
        )
        println(response1)
        println(response2)

        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testUpdRegistration() {
        val expectedCode1 = 704
        val expectedCode2 = 710
        val response1 = pushSDK.updateRegistration()
        val response2 = apiHandler.updateRegistration(
            "auth_token",
            "session_id",
            "sdk_version",
            "fcm_token",
            "device_type"
        )
        println(response1)
        println(response2)
        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testClearRegistration() {
        val expectedCode1 = 710
        val expectedCode2 = 704
        val expectedCode3 = 704
        val response = apiHandler.unregisterDevice(
            "[device, list]",
            "test_session_id",
            "test_api_key",

            )
        val response2 = pushSDK.unregisterCurrentDevice()
        val response3 = pushSDK.unregisterAllDevices()
        println(response)
        println(response2)
        println(response3)
        assertEquals(expectedCode1, response.code)
        assertEquals(expectedCode2, response2.code)
        assertEquals(expectedCode3, response3.code)
    }

    @Test
    @Throws(Exception::class)
    fun testSendDR() {
        val expectedCode1 = 704
        val expectedCode2 = 710
        val response1 = pushSDK.sendMessageDeliveryReport(
            "message_id"
        )
        val response2 = apiHandler.hMessageDr("message_id", "session_id", "auth_token")
        println(response1)
        println(response2)
        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testCallback() {
        val expectedCode1 = 710
        val expectedCode2 = 704
        val response1 = apiHandler.messageCallback(
            "message_id",
            "call_back",
            "test_session_id",
            "test_api_key"

        )
        val response2 = pushSDK.sendMessageCallback("message_id", "call_back")
        println(response1)
        println(response2)
        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testGetHistory() {
        val expectedCode1 = 710
        val expectedCode2 = 704
        val response1 = apiHandler.getHistory(
            "test_session_id",
            "test_api_key",
            640880

        )
        val response2 = pushSDK.getMessageHistory(640880)
        println(response1)
        println(response2)
        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testCheckQueue() {
        val expectedCode1 = 710
        val expectedCode2 = 704
        val response1 = apiHandler.getDevicePushMsgQueue(
            "test_session_id",
            "test_api_key"
        )
        val response2 = pushSDK.checkMessageQueue()
        println(response1)
        println(response2)
        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testGetDevices() {
        val expectedCode1 = 710
        val expectedCode2 = 704
        val response1 = apiHandler.getDeviceAll(
            "test_session_id",
            "test_api_key",
        )
        val response2 = pushSDK.getAllRegisteredDevices()
        println(response1)
        println(response2)
        assertEquals(expectedCode1, response1.code)
        assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testGetDeviceName() {
        println(Info.getDeviceName())
    }

    @Test
    @Throws(Exception::class)
    fun testGetPhoneType() {
        println(Info.getPhoneType(context))
    }

}