package com.gms_worldwide.push.app.service

import com.gms_worldwide.push.app.db.DBHelper
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.models.PushAnswerRegister


/**
 * Message and PushAnswerRegister service
 * @param dbHelper SQLiteOpenHelper contains CRUD functions for Message and PushAnswerRegister.
 */
class Service( val dbHelper: DBHelper) {

    /**
     * Add Message to DB.
     * @param message Message.
     */
    fun saveMessage(message: Message) {
        dbHelper.addMessage(message)
    }

    /**
     * Update Message in DB
     * @param message Message.
     */
    fun updateMessage(message: Message) {
        dbHelper.updateMessage(message)
    }

    /**
     * Get all Message from DB.
     * @return MutableList of Message.
     */
     fun getMessages(): MutableList<Message> {
        return dbHelper.messagesList
    }

    /**
     * Delete Message from DB.
     * @param message Message.
     */
    fun deleteMessage(message: Message) {
        dbHelper.deleteMessage(message)
    }


    /**
     * Save PushAnswerRegister in DB
     * @param registerData PushAnswerRegister.
     *
     * Check if there are registerData in DB and delete them. After that save new registerData.
     */
    fun saveRegisterDataInDB(registerData: PushAnswerRegister) {
        val allRegisterDataFromDB = getRegisterData()
        if (allRegisterDataFromDB.isEmpty()) {
            dbHelper.addRegisterData(registerData)
        } else {

            for (registerDataFromDB in allRegisterDataFromDB) {
                deleteRegisterData(registerDataFromDB)
            }
            dbHelper.addRegisterData(registerData)


        }
    }

    /**
     * Update PushAnswerRegister in DB
     * @param registerData PushAnswerRegister
     */
    fun updateRegisterData(registerData: PushAnswerRegister) {
        dbHelper.updateRegisterData(registerData)
    }


    /**
     * Get all PushAnswerRegister.
     * @return MutableList of PushAnswerRegister.
     */
    fun getRegisterData(): MutableList<PushAnswerRegister> {
        return dbHelper.registerList
    }

    /**
     * Delete PushAnswerRegister from DB.
     * @param registerData PushAnswerRegister
     */
    fun deleteRegisterData(registerData: PushAnswerRegister) {
        dbHelper.deleteRegisterData(registerData)
    }

    /**
     * Get message by ID from DB.
     * @param id of message in long.
     * @return single Message.
     */
    fun getMessageById(id: Long): Message {
        var result: Message
        result = Message("", "", "", "", "")
        val messages = getMessages()
        for (message in messages) {
            if (message.id == id) {
                result = message
            }
        }

        return result
    }

    /**
     * Checks by messageId if message is already in DB.
     * @param message
     * @return Boolean
     */
    fun isMessageAlreadyInDB(message: Message): Boolean {
        var messages = getMessages()
        if (messages.size > 0) {
            for (mss in messages) {
                if (message.messageId.equals(mss.messageId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the device is registered in DB.
     * @return Boolean
     */
    fun isDeviceRegistered(): Boolean {
        var result = true
        val deviceData = getRegisterData()
        if (deviceData == null || deviceData.isEmpty()){
            result = false
        }
        return result
    }

    /**
     * Delete all PushAnswerRegister from DB
     */
    fun deleteAllRegisterData() {
        val deviceData = getRegisterData()
        if (deviceData != null && !deviceData.isEmpty()){
            for (device in deviceData) {
                deleteRegisterData(device)
            }
        }
    }

}