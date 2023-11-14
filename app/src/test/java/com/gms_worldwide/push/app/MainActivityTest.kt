package com.gms_worldwide.push.app


import com.gms_worldwide.push.app.models.ImageModel
import com.gms_worldwide.push.app.models.Message
import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class MainActivityTest {

    lateinit var messages: ArrayList<Message>
    lateinit var testMss1: Message
    lateinit var testMss2: Message
    lateinit var testMss3: Message
    val testJsonStr =
        "{\"limitDays\": 92, \"limitMessages\": 1000, \"lastTime\": 1653312859, \"messages\": [{\"body\": \"Message Vodafonezzz\", \"time\": \"2022-05-23T09:24:50.901877+00\", \"image\": {\"url\": \"https://imgbox.com/Su9pJAPT\"}, \"phone\": \"380504886049\", \"title\": \"title Vodafone\", \"button\": {}, \"partner\": \"push\", \"messageId\": \"31f0d45d-da7a-11ec-a19a-000c29cec73c\"}, {\"body\": \"Message Vodafonexxx\", \"time\": \"2022-05-23T09:24:09.968893+00\", \"image\": {\"url\": \"https://images2.imgbox.com/46/cc/Su9pJAPT_o.png\"}, \"phone\": \"380504886049\", \"title\": \"title Vodafone\", \"button\": {}, \"partner\": \"push\", \"messageId\": \"18e362c8-da7a-11ec-a19a-000c29cec73c\"}]}"

    @Before
    fun before() {
        messages = ArrayList()
        var image = ImageModel("https://imgbox.com/Su9pJAPT")
        var image2 = ImageModel("https://images2.imgbox.com/46/cc/Su9pJAPT_o.png")
        testMss1 = Message(
            "title Vodafone",
            "Message Vodafonezzz",
            "31f0d45d-da7a-11ec-a19a-000c29cec73c",
            "2022-05-23T09:24:50.901877+00",
            "380504886049",
            1,
            image
        )
        testMss2 = Message(
            "title Vodafone",
            "Message Vodafonexxx",
            "18e362c8-da7a-11ec-a19a-000c29cec73c",
            "2022-05-23T09:24:09.968893+00",
            "380504886049",
            2,
            image2
        )
        testMss3 = Message("title3", "body3", "message id 3", "time3", "phone3", 3)

    }

    @Test
    fun isMessageAlreadyInListTest() {
        var result = false
        messages.add(testMss1)
        messages.add(testMss2)
        for (mss in messages) {
            if (testMss1.messageId.equals(mss.messageId)) {
                result = true
            }
        }

        assertEquals(true, result)

        result = false
        for (mss in messages) {
            if (testMss3.messageId.equals(mss.messageId)) {
                result = true
            }
        }
        assertEquals(false, result)
    }

    @Test
    fun getMessagesTest() {
        var messagesStr = testJsonStr

        var list = ArrayList<Message>()
        list.add(testMss1)
        list.add(testMss2)
        messagesStr = messagesStr.split("\"messages\":")[1].trim()

        messagesStr = messagesStr.substring(0, messagesStr.length - 1)

        var result = Gson().fromJson(messagesStr, Array<Message>::class.java)


        assertEquals(list, result.asList())
    }
}