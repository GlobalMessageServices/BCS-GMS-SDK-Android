package com.push.android.pushsdkandroid.settings


/**
 * The SDK's bubble settings; Used to set up custom bubble settings
 *
 *
 * @param isDefaultBubbleIconUsed Sets whether bubbleIconResourceId will be used or image from push message;
 * default value is true
 * if set to false and push message does not contain image, bubbleIconResourceId will be used
 * @param setImportant Sets whether this is important Person the push message is sent from;
 * default value is true
 * @param shortLabel Sets the custom short title of a shortcut;
 * default value is 'PushSDK chat'
 * @param setLongLived Sets if a shortcut would be valid even if it has been unpublished/invisible by the app;
 * default value is true
 * @param setAutoExpandBubble If set and the app creating the bubble is in the foreground, the bubble will be posted in its expanded state;
 * default value is true
 * @param setSuppressNotification If set the bubble will be posted without the associated notification in the notification shade;
 * default value is false
 * @param setDesiredHeight Sets the desired height in DPs for the app content defined by setIntent,
 * this height may not be respected if there is not enough space on the screen or if the provided height is too small to be useful
 */

open class BubbleSettings(
    isDefaultBubbleIconUsed: Boolean = true,
    setImportant: Boolean = true,
    shortLabel: String = "PushSDK chat",
    setLongLived: Boolean = true,
    setAutoExpandBubble: Boolean = true,
    setSuppressNotification: Boolean = false,
    setDesiredHeight: Int = 600
) {

    var isDefaultBubbleIconUsed: Boolean
    var isImportant: Boolean
    var shortLabel: String
    var isLongLived: Boolean
    var isAutoExpandBubble: Boolean
    var isSuppressNotification: Boolean
    var desiredHeight: Int

    init {
        this.isDefaultBubbleIconUsed = isDefaultBubbleIconUsed
        this.isImportant = setImportant
        this.shortLabel = shortLabel
        this.isLongLived = setLongLived
        this.isAutoExpandBubble = setAutoExpandBubble
        this.isSuppressNotification = setSuppressNotification
        this.desiredHeight = setDesiredHeight
    }
}