import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.core.APIHandler
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager
import com.push.android.pushsdkandroid.models.PushDataMessageImageModel
import com.push.android.pushsdkandroid.models.PushDataMessageModel
import com.push.android.pushsdkandroid.utils.Info
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PushSDKAndroidTest {

    private lateinit var appContext: Context
    private lateinit var pushSDK: PushSDK
    private lateinit var apiHandler: APIHandler
    private lateinit var notificationManager: PushSdkNotificationManager
    private lateinit var message: String
    private lateinit var remoteMessage: RemoteMessage
    private val map: HashMap<String, String> = HashMap()

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        pushSDK = PushSDK(appContext, "https://example.io/api/3.0/")
        apiHandler = APIHandler(appContext)
        notificationManager = PushSdkNotificationManager(appContext, Pair("source", "message"))

        message = "{\"button\":{},\"image\":{\"url\":\"https://test_image.jpg\"},\"partner\":\"push\",\"phone\":\"0123456789\",\"messageId\":\"6c000000-4b8f-11ed-972a-0000006010000\",\"time\":\"2022-06-22T07:34:53.738326+00\",\"body\":\"Test Message Android\",\"title\":\"Test title Android\"}"
        map["source"] = "Messaging HUB"
        map["message"] = message.toString()
        remoteMessage = RemoteMessage.Builder("internal").setData(map).build()
    }

    @Test
    @Throws(Exception::class)
    fun testConstructNotification() {
       val construct1 = notificationManager.constructNotification(
            remoteMessage.data,
            PushSdkNotificationManager.NotificationStyle.NO_STYLE
        )

        val construct2 = notificationManager.constructNotification(
            remoteMessage.data,
            PushSdkNotificationManager.NotificationStyle.BIG_TEXT
        )

        val construct3 = notificationManager.constructNotification(
            remoteMessage.data,
            PushSdkNotificationManager.NotificationStyle.BIG_PICTURE
        )
        println(construct1)
        println(construct2)
        println(construct3)

        val isSent = construct2?.let { notificationManager.sendNotification(it) }

        println(isSent)
    }

    @Test
    @Throws(Exception::class)
    fun testIsChannelMuted() {
        val result = notificationManager.isNotificationChannelMuted(PushSdkNotificationManager.DEFAULT_NOTIFICATION_CHANNEL_ID)
        println(result)
    }

    @Test
    @Throws(Exception::class)
    fun testDisturbModeActive() {
        val result = notificationManager.isDoNotDisturbModeActive()
        println(result)
    }

    @Test
    @Throws(Exception::class)
    fun testNotificationsEnabled() {
        val result = notificationManager.areNotificationsEnabled()
        println(result)
    }

    @Test
    @Throws(Exception::class)
    fun testSpaceForNotification() {
        val result = notificationManager.hasSpaceForNotification(false)
        println(result)
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

        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
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
        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
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
        TestCase.assertEquals(expectedCode1, response.code)
        TestCase.assertEquals(expectedCode2, response2.code)
        TestCase.assertEquals(expectedCode3, response3.code)
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
        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
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
        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
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
        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
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
        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
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
        TestCase.assertEquals(expectedCode1, response1.code)
        TestCase.assertEquals(expectedCode2, response2.code)
    }

    @Test
    @Throws(Exception::class)
    fun testGetDeviceName() {
        println(Info.getDeviceName())
    }

    @Test
    @Throws(Exception::class)
    fun testGetPhoneType() {
        println(Info.getPhoneType(appContext))
    }
}


internal data class MessageModel(
    val messageId: String,
    val title: String,
    val body: String,
    val image: MessageImageModel,
)

internal data class MessageImageModel(
    val url: String
)