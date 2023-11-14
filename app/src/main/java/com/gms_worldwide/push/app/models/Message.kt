package com.gms_worldwide.push.app.models



/**
 * Data class for push messages received from firebase.
 * @param title title of push message.
 * @param body text of push message.
 * @param messageId messageId of push message provided by push server.
 * @param time time when the push message was sent.
 * @param phone phone to which the push message was sent.
 * @param id id of push message for DB. This parameter is not contained in a message from firebase.
 */

data class Message(
    val title: String,
    var body: String,
    val messageId: String,
    val time: String,
    val phone: String,
    val id: Long = 0,
    val image: ImageModel? = null
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (title != other.title) return false
        if (body != other.body) return false
        if (messageId != other.messageId) return false
        if (time != other.time) return false
        if (phone != other.phone) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        return result
    }
}

data class ImageModel(
    val url: String
)
