package com.gms_worldwide.push.app.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.models.PushAnswerRegister

/**
 * DBHelper extends SQLiteOpenHelper. DBHelper SQL DB, which contains CRUD functions for Message and PushAnswerRegister.
 * @param context
 */
class DBHelper(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VER) {

    /**
     * Constants variables.
     */
    companion object {
        private const val DATABASE_VER = 2
        private val DATABASE_NAME = "push_app.db"

        //Table1
        private const val TABLE_NAME = "push_message"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_BODY = "body"
        private const val COL_MESSAGE_ID = "messageId"
        private const val COL_TIME = "time"
        private const val COL_PHONE = "phone"

        //Table2
        private const val TABLE_NAME_REG = "registration_answer"
        private const val COL_ID_REG = "id"
        private const val COL_DEVICE_ID = "deviceId"
        private const val COL_TOKEN = "token"
        private const val COL_USER_ID = "userId"
        private const val COL_USER_PHONE = "userPhone"
        private const val COL_CREATED = "createdAt"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        p0!!.execSQL("CREATE TABLE  $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $COL_TITLE  TEXT, $COL_BODY TEXT, $COL_MESSAGE_ID TEXT, $COL_TIME TEXT, $COL_PHONE TEXT)")
        p0.execSQL("CREATE TABLE  $TABLE_NAME_REG ($COL_ID_REG INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $COL_DEVICE_ID  TEXT, $COL_TOKEN TEXT,$COL_USER_ID TEXT, $COL_USER_PHONE TEXT, $COL_CREATED TExt)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_REG")
        onCreate(p0)
    }


    /**
     * MutableList of all Message in DB.
     */
    val messagesList: MutableList<Message>
        @SuppressLint("Range")
        get() {
            val messages = ArrayList<Message>()
            val selectQuery = "SELECT * FROM $TABLE_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val message = Message(
                        cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COL_BODY)),
                            cursor.getString(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_TIME)),
                        cursor.getString(cursor.getColumnIndex(COL_PHONE)),
                        cursor.getLong(cursor.getColumnIndex(COL_ID))

                    )
                    messages.add(message)
                } while (cursor.moveToNext())
            }
            db.close()
            cursor.close()
            return messages
        }

    /**
     * Add Message to DB.
     * @param message  Message.
     */
    fun addMessage(message: Message) {
        val db = this.writableDatabase
        val values = ContentValues()
        //values.put(COL_ID, message.id)
        values.put(COL_TITLE, message.title)
        values.put(COL_BODY, message.body)
        values.put(COL_MESSAGE_ID, message.messageId)
        values.put(COL_TIME, message.time)
        values.put(COL_PHONE, message.phone)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    /**
     * Update Message in DB.
     * @param message  Message.
     */
    fun updateMessage(message: Message): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_ID, message.id)
        values.put(COL_TITLE, message.title)
        values.put(COL_BODY, message.body)
        values.put(COL_MESSAGE_ID, message.messageId)
        values.put(COL_TIME, message.time)
        values.put(COL_PHONE, message.phone)

        return db.update(TABLE_NAME, values, "$COL_ID=?", arrayOf(message.id.toString()))

    }

    /**
     * Delete Message from DB.
     * @param message  Message.
     */
    fun deleteMessage(message: Message) {
        val db = this.writableDatabase


        db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(message.id.toString()))
        db.close()
    }

    /**
     * MutableList of all PushAnswerRegister in DB.
     */
    val registerList: MutableList<PushAnswerRegister>
        @SuppressLint("Range")
        get() {
            val registerDataList = ArrayList<PushAnswerRegister>()
            val selectQuery = "SELECT * FROM $TABLE_NAME_REG"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val registerData = PushAnswerRegister(
                        cursor.getString(cursor.getColumnIndex(COL_DEVICE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_TOKEN)),
                        cursor.getString(cursor.getColumnIndex(COL_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_USER_PHONE)),
                        cursor.getString(cursor.getColumnIndex(COL_CREATED)),
                        cursor.getLong(cursor.getColumnIndex(COL_ID_REG))

                    )
                    registerDataList.add(registerData)
                } while (cursor.moveToNext())
            }
            db.close()
            cursor.close()
            return registerDataList
        }

    /**
     * Add PushAnswerRegister to DB.
     * @param registerData  PushAnswerRegister.
     */
    fun addRegisterData(registerData: PushAnswerRegister) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_ID_REG, registerData.id)
        values.put(COL_DEVICE_ID, registerData.deviceId)
        values.put(COL_TOKEN, registerData.token)
        values.put(COL_USER_ID, registerData.userId)
        values.put(COL_USER_PHONE, registerData.userPhone)
        values.put(COL_CREATED, registerData.createdAt)

        db.insert(TABLE_NAME_REG, null, values)
        db.close()
    }

    /**
     * Update PushAnswerRegister in DB.
     * @param registerData  PushAnswerRegister.
     */
    fun updateRegisterData(registerData: PushAnswerRegister): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_ID_REG, registerData.id)
        values.put(COL_DEVICE_ID, registerData.deviceId)
        values.put(COL_TOKEN, registerData.token)
        values.put(COL_USER_ID, registerData.userId)
        values.put(COL_USER_PHONE, registerData.userPhone)
        values.put(COL_CREATED, registerData.createdAt)

        return db.update(TABLE_NAME_REG, values, "$COL_ID_REG=?", arrayOf(registerData.id.toString()))

    }

    /**
     * Delete PushAnswerRegister from DB.
     * @param registerData  PushAnswerRegister.
     */
    fun deleteRegisterData(registerData: PushAnswerRegister) {
        val db = this.writableDatabase


        db.delete(TABLE_NAME_REG, "$COL_ID_REG=?", arrayOf(registerData.id.toString()))
        db.close()
    }

}