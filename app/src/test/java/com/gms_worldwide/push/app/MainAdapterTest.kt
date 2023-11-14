package com.gms_worldwide.push.app


import android.os.Build
import androidx.annotation.RequiresApi
import com.gms_worldwide.push.app.models.Message
import junit.framework.TestCase.assertEquals

import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import kotlin.collections.ArrayList


class MainAdapterTest {

    lateinit var messages: ArrayList<Message>
    lateinit var testMss1: Message
    lateinit var testMss2: Message
    lateinit var testMss3: Message

    lateinit var timeZone : TimeZone

    @Before
    fun before(){
        timeZone = TimeZone.getTimeZone("Europe/Oslo")


        messages = ArrayList()
        testMss1 = Message(
            "title Vodafone",
            "Message Vodafonezzz",
            "31f0d45d-da7a-11ec-a19a-000c29cec73c",
            "2022-05-23T09:24:50.901877+00",
            "380504886049",
            1

        )
        testMss2 = Message(
            "title Vodafone",
            "Message Vodafonexxx",
            "18e362c8-da7a-11ec-a19a-000c29cec73c",
            "2022-05-23T09:24:51.968893+00",
            "380504886049",
            2

        )
        testMss3 = Message("title Vodafone",
            "Message Vodafonexxx",
            "18e362c8-da7a-11ec-a19a-000c29cec73c",
            "2022-05-23T09:24:10.968893+00",
            "380504886049",
            3)
    }


    @Test
    @Throws(Exception::class)
    fun getFormattedDateTest(){
        val testDate1 = "2022-05-23T09:24:09.968893+00"
        val testDate2 = "2022-05-23T09:24:09"
        val testDate3 = "  "
        val expected1 = "23 May 2022 11:24"
        val expected2 = "23 May 2022 11:24"
        val expected3 = "  "


        assertEquals(expected1, getFormattedDate(testDate1))
        assertEquals(expected2, getFormattedDate(testDate2))
        assertEquals(expected3, getFormattedDate(testDate3))
    }


    @Test
    @Throws(Exception::class)
    fun getLastMessageTest(){

        messages.add(testMss1)
        messages.add(testMss2)
        messages.add(testMss3)

        assertEquals(testMss2, getLastMessage(messages))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFormattedDate(time: String): String {

        return if (time?.trim().isNotEmpty()) {
            try {
                val timeStr = time.split(".")[0]
                var date = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val zonedUTC = date.atZone(ZoneId.of("UTC"))
                date = zonedUTC.withZoneSameInstant(ZoneId.of(timeZone.getID())).toLocalDateTime()
                date.format(DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm"))

            } catch (e: Exception) {
                e.printStackTrace()
                time
            }

        } else {
            time
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastMessage(messages: List<Message>): Message {
        var result = Message("", "", "", "", "");
        var lastDate = LocalDateTime.of(1990, 1, 1, 0, 0)
        for (message in messages) {
            val timeStr = message.time.split(".")[0]
            var date = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            if (lastDate.isBefore(date)) {
                result = message
                lastDate = date
            }

        }

        return result;
    }
}