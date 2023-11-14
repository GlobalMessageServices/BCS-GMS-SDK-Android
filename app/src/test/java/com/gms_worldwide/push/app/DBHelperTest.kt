package com.gms_worldwide.push.app

import com.gms_worldwide.push.app.db.DBHelper
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.models.PushAnswerRegister
import junit.framework.TestCase.assertEquals

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment


@RunWith(RobolectricTestRunner::class)
class DBHelperTest {

    lateinit var dbHelper: DBHelper

    lateinit var testMss1: Message
    lateinit var testMss2: Message
    lateinit var testMss3: Message
    lateinit var testAnswer1: PushAnswerRegister
    lateinit var testAnswer2: PushAnswerRegister
    lateinit var testAnswer3: PushAnswerRegister


    @Before
    fun setup() {
        dbHelper = DBHelper(RuntimeEnvironment.getApplication())
        testMss1 = Message("title1", "body1", "message id 1", "time1", "phone1", 1)
        testMss2 = Message("title2", "body2", "message id 2", "time2", "phone2", 2)
        testMss3 = Message("title3", "body3", "message id 3", "time3", "phone3", 3)
        testAnswer1 =
            PushAnswerRegister("device id1", "token1", "user id1", "user phone1", "time 1", 1)
        testAnswer2 =
            PushAnswerRegister("device id2", "token2", "user id2", "user phone2", "time 2", 2)
        testAnswer3 =
            PushAnswerRegister("device id3", "token3", "user id3", "user phone3", "time 2", 3)

    }

    @Test
    @Throws(Exception::class)
    fun testDbInsertion() {

        // Given
        val list = ArrayList<Message>()
        list.add(testMss1)
        list.add(testMss2)

        // When
        dbHelper.addMessage(testMss1)
        dbHelper.addMessage(testMss2)

        // Then
        assertEquals(list, dbHelper.messagesList)
    }

    @Test
    @Throws(Exception::class)
    fun testDbDeleting() {
        val list = ArrayList<Message>()
        list.add(testMss1)
        list.add(testMss3)
        dbHelper.addMessage(testMss1)
        dbHelper.addMessage(testMss2)
        dbHelper.addMessage(testMss3)

        dbHelper.deleteMessage(testMss2)
        assertEquals(list, dbHelper.messagesList)
    }

    @Test
    @Throws(Exception::class)
    fun testDbUpdating() {
        dbHelper.addMessage(testMss1)
        dbHelper.addMessage(testMss3)
        testMss1.body = "updated body 1"

        val list = ArrayList<Message>()
        list.add(testMss1)
        list.add(testMss3)

        dbHelper.updateMessage(testMss1)

        assertEquals(list, dbHelper.messagesList)
    }


    @Test
    @Throws(Exception::class)
    fun testDbAnswerInsertion() {

        // Given
        val list = ArrayList<PushAnswerRegister>()
        list.add(testAnswer1)
        list.add(testAnswer2)

        // When
        dbHelper.addRegisterData(testAnswer1)
        dbHelper.addRegisterData(testAnswer2)

        // Then
        assertEquals(list, dbHelper.registerList)
    }

    @Test
    @Throws(Exception::class)
    fun testDbAnswerDeleting() {
        val list = ArrayList<PushAnswerRegister>()
        dbHelper.addRegisterData(testAnswer1)
        dbHelper.addRegisterData(testAnswer2)
        dbHelper.addRegisterData(testAnswer3)
        list.add(testAnswer1)
        list.add(testAnswer3)

        dbHelper.deleteRegisterData(testAnswer2)
        assertEquals(list, dbHelper.registerList)
    }

    @Test
    @Throws(Exception::class)
    fun testDbAnswerUpdating() {
        dbHelper.addRegisterData(testAnswer1)
        dbHelper.addRegisterData(testAnswer3)
        testAnswer1.token = "updated token 1"

        val list = ArrayList<PushAnswerRegister>()
        list.add(testAnswer1)
        list.add(testAnswer3)

        dbHelper.updateRegisterData(testAnswer1)

        assertEquals(list, dbHelper.registerList)
    }


    @After
    fun tearDown() {
        // dbHelper.clearDb()
    }
}