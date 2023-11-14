package com.gms_worldwide.push.app.models



internal data class MessageList(
    val limitDays: String,
    val limitMessages: String,
    val lastTime : String,
    val messages: List<PushDataMessageModel>
)

internal data class PushDataMessageModel(
    val messageId: String,
    val title: String,
    val body: String,
    val image: PushDataMessageImageModel,
    val button: PushDataMessageButtonModel,
    val is2Way: Boolean
)


internal data class PushDataMessageImageModel(
    val url: String
)


internal data class PushDataMessageButtonModel(
    val text: String,
    val url: String
)