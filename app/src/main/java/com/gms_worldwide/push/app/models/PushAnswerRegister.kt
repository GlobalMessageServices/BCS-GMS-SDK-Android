package com.gms_worldwide.push.app.models

import java.io.Serializable

/**
 * Data class for registration answers from push server.
 * @param deviceId id provided to the device during registration.
 * @param token
 * @param userId id provided to the user during registration.
 * @param userPhone registered phone number.
 * @param createdAt date of registration.
 * @param deviceId id for local DB. This parameter is not contained in registration request response.
 */

data class PushAnswerRegister(
    val deviceId: String = "",
    var token: String = "",
    val userId: String = "",
    val userPhone: String = "",
    val createdAt: String = "",
    val id: Long = 0
)  {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PushAnswerRegister

        if (deviceId != other.deviceId) return false
        if (token != other.token) return false
        if (userId != other.userId) return false
        if (userPhone != other.userPhone) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + userPhone.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}