import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.core.APIHandler
import com.push.android.pushsdkandroid.core.SharedPreferencesHandler
import com.push.android.pushsdkandroid.managers.PushSdkNotificationManager
import com.push.android.pushsdkandroid.settings.BubbleSettings
import com.push.android.pushsdkandroid.utils.Info
import junit.framework.TestCase
import org.junit.Assert.assertEquals
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
    private lateinit var message2: String
    private lateinit var message3: String
    private lateinit var remoteMessage: RemoteMessage
    private lateinit var remoteMessage2: RemoteMessage
    private lateinit var remoteMessage3: RemoteMessage
    private val map: HashMap<String, String> = HashMap()
    private val map2: HashMap<String, String> = HashMap()
    private val map3: HashMap<String, String> = HashMap()
    private lateinit var sharedPreferencesHandler: SharedPreferencesHandler

    @Before
    fun before() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        pushSDK = PushSDK(appContext, "https://example.io/api/3.0/")
        apiHandler = APIHandler(appContext)
        notificationManager = PushSdkNotificationManager(appContext, Pair("source", "message"))
        sharedPreferencesHandler = SharedPreferencesHandler(appContext)
        message =
            "{\"button\":{},\"image\":{\"url\":\"https://test_image.jpg\"},\"partner\":\"push\",\"phone\":\"0123456789\",\"messageId\":\"6c000000-4b8f-11ed-972a-0000006010000\",\"time\":\"2022-06-22T07:34:53.738326+00\",\"body\":\"Test Message Android\",\"title\":\"Test title Android\"}"
        message2 =
            "{\"button\":{\"text\":\"click\",\"url\":\"https://www.gms-worldwide.com/\"},\"image\":{\"url\":\"https://test_image.jpg\"},\"partner\":\"push\",\"phone\":\"0123456789\",\"messageId\":\"6c000000-4b8f-11ed-972a-0000006010000\",\"time\":\"2022-06-22T07:34:53.738326+00\",\"body\":\"Test Message Android\",\"title\":\"Test title Android\"}"
        map["source"] = "Messaging HUB"
        map["message"] = message
        remoteMessage = RemoteMessage.Builder("internal").setData(map).build()
        map2["source"] = "Messaging HUB"
        map2["message"] = message2
        remoteMessage2 = RemoteMessage.Builder("internal").setData(map2).build()
        message3 =
            "{\"button\":{},\"image\":{\"url\":\"https://test_image.jpg\"},\"partner\":\"push\",\"phone\":\"0123456789\",\"messageId\":\"6c000000-4b8f-11ed-972a-0000006010000\",\"time\":\"2022-06-22T07:34:53.738326+00\",\"body\":\"Test Message Android\",\"title\":\"Test title Android\", \"is2Way\":true}"
        map3["source"] = "Messaging HUB"
        map3["message"] = message3
        remoteMessage3 = RemoteMessage.Builder("internal").setData(map3).build()
    }


    @Test
    @Throws(Exception::class)
    fun testSharedPreferences() {
        var testString = "some string"
        var testInt = 123456
        var testBool = true

        sharedPreferencesHandler.save("TEST_INT", testInt)
        sharedPreferencesHandler.save("TEST_BOOL", testBool)
        sharedPreferencesHandler.saveString("TEST_STRING", testString)

        val testStrDB = sharedPreferencesHandler.getValueString("TEST_STRING")
        val testIntDB = sharedPreferencesHandler.getValueInt("TEST_INT")
        val testBoolDB = sharedPreferencesHandler.getValueBool("TEST_BOOL", false)

        TestCase.assertEquals(testString, testStrDB)
        TestCase.assertEquals(testInt, testIntDB)
        TestCase.assertEquals(testBool, testBoolDB)
    }

    @Test
    @Throws(Exception::class)
    fun testConstructNotification() {

        val notificationId = notificationManager.getNotificationId()
        val notificationIdBubbles = notificationManager.getNotificationId()

        //Construct without style
        val construct1 = notificationManager.constructNotification(
            remoteMessage.data,
            notificationManager.getNotificationId(),
            PushSdkNotificationManager.NotificationStyle.NO_STYLE
        )

        //Construct with big text style
        val construct2 = notificationManager.constructNotification(
            remoteMessage.data,
            notificationId,
            PushSdkNotificationManager.NotificationStyle.BIG_TEXT
        )

        //Construct with big picture style
        val construct3 = notificationManager.constructNotification(
            remoteMessage.data,
            notificationManager.getNotificationId(),
            PushSdkNotificationManager.NotificationStyle.BIG_PICTURE
        )

        //Construct with big text style (with button)
        val construct4 = notificationManager.constructNotification(
            remoteMessage2.data,
            notificationManager.getNotificationId(),
            PushSdkNotificationManager.NotificationStyle.BIG_TEXT
        )


        //Construct with bubble style
        val constructBubble = notificationManager.constructNotification(
            remoteMessage2.data,
            notificationIdBubbles,
            PushSdkNotificationManager.NotificationStyle.BUBBLES,
            bubbleIntent = Intent(),
            bubbleSettings = BubbleSettings()
        )

        val construct2Way = notificationManager.constructNotification(
            remoteMessage3.data,
            notificationManager.getNotificationId(),
            PushSdkNotificationManager.NotificationStyle.BIG_TEXT
        )

        println(construct1)
        println(construct2)
        println(construct3)
        println(construct4)
        println(constructBubble)
        println(construct2Way)

        val isSent = construct2?.let { notificationManager.sendNotification(it, notificationId) }
        val isSent2wWay =
            construct2Way?.let { notificationManager.sendNotification(it, notificationId) }

        val isSentBubble =
            constructBubble?.let { notificationManager.sendNotification(it, notificationIdBubbles) }




        println(isSent)
        println(isSent2wWay)
        println(isSentBubble)

        assertEquals(true, isSent)
        assertEquals(true, isSent2wWay)

    }

    @Test
    @Throws(Exception::class)
    fun testIsChannelMuted() {
        val result =
            notificationManager.isNotificationChannelMuted(PushSdkNotificationManager.DEFAULT_NOTIFICATION_CHANNEL_ID)
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

    @Test
    @Throws(Exception::class)
    fun testGetUserData() {
        println(pushSDK.getUserData())
    }
}