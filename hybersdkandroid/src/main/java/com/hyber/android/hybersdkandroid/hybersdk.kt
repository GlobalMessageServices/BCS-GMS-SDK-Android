package com.hyber.android.hybersdkandroid

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.hyber.android.hybersdkandroid.add.Answer
import com.hyber.android.hybersdkandroid.add.GetInfo
import com.hyber.android.hybersdkandroid.add.PushParsing
import com.hyber.android.hybersdkandroid.add.RewriteParams
import com.hyber.android.hybersdkandroid.core.*
import com.hyber.android.hybersdkandroid.logger.PushKLoggerSdk
import kotlin.properties.Delegates

@Suppress("SpellCheckingInspection")
object HyberPushMess {
    var message: String? = null   //global variable for push messages
    var log_level_active: String = "error" //global variable sdk log level
    var push_message_style: Int = 1 //style type of push notification
    //push_message_style types
    //0 - only text in push notification
    //1 - text and large image in notification
}

internal lateinit var PushKDatabase: PushOperativeData

@Suppress("SpellCheckingInspection", "unused", "FunctionName")
class HyberSDKQueue {

    fun hyber_check_queue(context: Context): HyberFunAnswerGeneral {
        val answerNotKnown = HyberFunAnswerGeneral(710, "Failed", "Unknown error", "unknown")
        try {
            val answ = Answer()
            val answerNotRegistered = HyberFunAnswerGeneral(
                704,
                "Failed",
                "Registration data not found",
                "Not registered"
            )
            val initPushParams2 = Initialization(context)
            initPushParams2.hSdkGetParametersFromLocal()

            return if (PushKDatabase.registrationStatus) {
                val queue = QueueProc()
                val anss = queue.pushDeviceMessQueue(
                    PushKDatabase.firebase_registration_token,
                    PushKDatabase.push_k_registration_token, context
                )
                PushKLoggerSdk.debug("PushSDKQueue.hyber_check_queue response: $anss")
                answ.generalAnswer("200", "{}", "Success")
            } else {
                answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }
}

@Suppress("SpellCheckingInspection", "unused", "FunctionName", "MemberVisibilityCanBePrivate")
class HyberSDK(
    context: Context,
    platform_branch: UrlsPlatformList = PushSdkParametersPublic.branchMasterValue,
    log_level: String = "error",
    push_style: Int = 0
) {

    //any classes initialization
    private var context: Context by Delegates.notNull()
    private var initHObject: Initialization = Initialization(context)
    private var localDeviceInfo: GetInfo = GetInfo()
    private var apiPushData: PushKApi = PushKApi()
    private var answerAny: Answer = Answer()
    private var rewriteParams: RewriteParams = RewriteParams(context)
    private var parsing: PushParsing = PushParsing()
    private var pushInternalParamsObject: PushSdkParameters = PushSdkParameters
    private var pushDeviceType: String = ""

    //main class initialization
    init {
        this.context = context
        HyberPushMess.log_level_active = log_level
        HyberPushMess.push_message_style = push_style
        pushDeviceType = localDeviceInfo.getPhoneType(context)
        PushSdkParameters.branch_current_active = platform_branch
        try {
            val localDataLoaded = initHObject.hSdkGetParametersFromLocal()
            if (localDataLoaded.registrationStatus) {
                this.hyber_update_registration()
            }
        } catch (e: Exception) {
            PushKLoggerSdk.error("PushSDK.init registration update problem $e")
        }
        updateToken()
    }


    private var answerNotRegistered: HyberFunAnswerGeneral =
        HyberFunAnswerGeneral(704, "Failed", "Registration data not found", "Not registered")
    private var answerNotKnown: HyberFunAnswerGeneral =
        HyberFunAnswerGeneral(710, "Failed", "Unknown error", "unknown")

    //answer codes
    //200 - Ok

    //answers from remote server
    //401 HTTP code – (Client error) authentication error, probably errors
    //400 HTTP code – (Client error) request validation error, probably errors
    //500 HTTP code – (Server error) 

    //sdk errors
    //700 - internal SDK error
    //701 - already exists
    //704 - not registered
    //705 - remote server error
    //710 - unknown error

    //network errors
    //901 - failed registration with firebase

    //{
    //    "result":"Ok",
    //    "description":"",
    //    "code":200,
    //    "body":{
    //}
    //}


    //1
    fun hyber_register_new(
        X_Hyber_Client_API_Key: String,
        X_Hyber_App_Fingerprint: String,
        user_msisdn: String,
        user_password: String
    ): HyberFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            val xPushSessionId = PushKDatabase.firebase_registration_token
            PushKLoggerSdk.debug("Start hyber_register_new X_Hyber_Client_API_Key: ${X_Hyber_Client_API_Key}, X_Hyber_App_Fingerprint: ${X_Hyber_App_Fingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Hyber_Session_Id: $xPushSessionId")

            if (PushKDatabase.registrationStatus) {
                return answerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )
            } else {

                if (xPushSessionId != "" && xPushSessionId != " ") {
                    val respPush: PushKDataApi2 = apiPushData.hDeviceRegister(
                        X_Hyber_Client_API_Key,
                        xPushSessionId,
                        X_Hyber_App_Fingerprint,
                        PushSdkParameters.push_k_deviceName,
                        pushDeviceType,
                        PushSdkParameters.push_k_osType,
                        PushSdkParameters.sdkVersion,
                        user_password,
                        user_msisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushKLoggerSdk.debug("hyber_register_new response: $respPush")
                    PushKLoggerSdk.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPush.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPush.body.deviceId,
                        user_msisdn,
                        user_password,
                        respPush.body.token,
                        respPush.body.userId,
                        respPush.body.createdAt,
                        regStatus
                    )
                    return HyberFunAnswerRegister(
                        code = respPush.code,
                        result = respPush.body.result,
                        description = respPush.body.description,
                        deviceId = respPush.body.deviceId,
                        token = respPush.body.token,
                        userId = respPush.body.userId,
                        userPhone = respPush.body.userPhone,
                        createdAt = respPush.body.createdAt
                    )
                } else {
                    return answerAny.registerProcedureAnswer2(
                        "901",
                        "X_Hyber_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return answerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }


    //1-1
    //registration procedure with direct FCM token input
    //hyber_register_new2(
    fun hyber_register_new(
        X_Hyber_Client_API_Key: String,    // APP API key on hyber platform
        X_Hyber_App_Fingerprint: String,   // App Fingerprint key
        user_msisdn: String,               // User MSISDN
        user_password: String,             // User Password
        X_FCM_token: String                // FCM firebase token
    ): HyberFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            PushKLoggerSdk.debug("Start hyber_register_new: X_Hyber_Client_API_Key: ${X_Hyber_Client_API_Key}, X_Hyber_App_Fingerprint: ${X_Hyber_App_Fingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Hyber_Session_Id: $X_FCM_token")

            if (PushKDatabase.registrationStatus) {
                return answerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )

            } else {
                initHObject.hSdkUpdateFirebaseManual(X_FCM_token)
                if (X_FCM_token != "" && X_FCM_token != " ") {
                    val respPush: PushKDataApi2 = apiPushData.hDeviceRegister(
                        X_Hyber_Client_API_Key,
                        X_FCM_token,
                        X_Hyber_App_Fingerprint,
                        PushSdkParameters.push_k_deviceName,
                        pushDeviceType,
                        PushSdkParameters.push_k_osType,
                        PushSdkParameters.sdkVersion,
                        user_password,
                        user_msisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushKLoggerSdk.debug("hyber_register_new response: $respPush")
                    PushKLoggerSdk.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPush.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPush.body.deviceId,
                        user_msisdn,
                        user_password,
                        respPush.body.token,
                        respPush.body.userId,
                        respPush.body.createdAt,
                        regStatus
                    )

                    return HyberFunAnswerRegister(
                        code = respPush.code,
                        result = respPush.body.result,
                        description = respPush.body.description,
                        deviceId = respPush.body.deviceId,
                        token = respPush.body.token,
                        userId = respPush.body.userId,
                        userPhone = respPush.body.userPhone,
                        createdAt = respPush.body.createdAt
                    )
                } else {
                    return answerAny.registerProcedureAnswer2(
                        "901",
                        "X_Hyber_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return answerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }

//deprecated
    @Deprecated("Function hyber_register_new2 will be remove soon. Please use hyber_register_new")
    fun hyber_register_new2(
        X_Hyber_Client_API_Key: String,    // APP API key on hyber platform
        X_Hyber_App_Fingerprint: String,   // App Fingerprint key
        user_msisdn: String,               // User MSISDN
        user_password: String,             // User Password
        X_FCM_token: String                // FCM firebase token
    ): HyberFunAnswerRegister {
        try {
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            PushKLoggerSdk.debug("Start hyber_register_new: X_Hyber_Client_API_Key: ${X_Hyber_Client_API_Key}, X_Hyber_App_Fingerprint: ${X_Hyber_App_Fingerprint}, registrationstatus: ${PushKDatabase.registrationStatus}, X_Hyber_Session_Id: $X_FCM_token")

            if (PushKDatabase.registrationStatus) {
                return answerAny.pushKRegisterNewRegisterExists2(
                    PushKDatabase.deviceId,
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.push_k_user_id,
                    PushKDatabase.push_k_user_msisdn,
                    PushKDatabase.push_k_registration_createdAt
                )

            } else {
                initHObject.hSdkUpdateFirebaseManual(X_FCM_token)
                if (X_FCM_token != "" && X_FCM_token != " ") {
                    val respPushK: PushKDataApi2 = apiPushData.hDeviceRegister(
                        X_Hyber_Client_API_Key,
                        X_FCM_token,
                        X_Hyber_App_Fingerprint,
                        PushSdkParameters.push_k_deviceName,
                        pushDeviceType,
                        PushSdkParameters.push_k_osType,
                        PushSdkParameters.sdkVersion,
                        user_password,
                        user_msisdn,
                        context
                    )
                    //rewriteParams.rewritePushUserMsisdn(user_msisdn)
                    //rewriteParams.rewritePushUserPassword(user_password)

                    PushKLoggerSdk.debug("hyber_register_new response: $respPushK")
                    PushKLoggerSdk.debug("uuid: ${PushKDatabase.push_k_uuid}")

                    var regStatus = false
                    if (respPushK.code == 200) {
                        regStatus = true
                    }

                    initHObject.hSdkInitSaveToLocal(
                        respPushK.body.deviceId,
                        user_msisdn,
                        user_password,
                        respPushK.body.token,
                        respPushK.body.userId,
                        respPushK.body.createdAt,
                        regStatus
                    )

                    return HyberFunAnswerRegister(
                        code = respPushK.code,
                        result = respPushK.body.result,
                        description = respPushK.body.description,
                        deviceId = respPushK.body.deviceId,
                        token = respPushK.body.token,
                        userId = respPushK.body.userId,
                        userPhone = respPushK.body.userPhone,
                        createdAt = respPushK.body.createdAt
                    )
                } else {
                    return answerAny.registerProcedureAnswer2(
                        "901",
                        "X_Hyber_Session_Id is empty. Maybe firebase registration problem",
                        context
                    )
                }
            }
        } catch (e: Exception) {
            return answerAny.registerProcedureAnswer2("700", "unknown", context)
        }
    }


    //2
    fun hyber_clear_current_device(): HyberFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("hyber_clear_current_device start")
            updateToken()
            initHObject.hSdkGetParametersFromLocal()
            val xPushSessionId = PushKDatabase.firebase_registration_token
            if (PushKDatabase.registrationStatus) {
                PushKLoggerSdk.debug("Start hyber_clear_current_device: firebase_registration_token: ${xPushSessionId}, hyber_registration_token: ${PushKDatabase.push_k_registration_token}, registrationstatus: ${PushKDatabase.registrationStatus}, deviceId: ${PushKDatabase.deviceId}")

                val pushAnswer: PushKDataApi = apiPushData.hDeviceRevoke(
                    "[\"${PushKDatabase.deviceId}\"]",
                    xPushSessionId,
                    PushKDatabase.push_k_registration_token
                )
                PushKLoggerSdk.debug("hyber_answer : $pushAnswer")

                if (pushAnswer.code == 200) {
                    PushKLoggerSdk.debug("start clear data")
                    val deviceId = PushKDatabase.deviceId
                    initHObject.clearData()
                    return answerAny.generalAnswer(
                        "200",
                        "{\"device\":\"$deviceId\"}",
                        "Success"
                    )
                } else {
                    if (pushAnswer.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answerAny.generalAnswer(
                        pushAnswer.code.toString(),
                        "{\"body\":\"unknown\"}",
                        "Some problem"
                    )
                }
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //return all message history till time
    //3
    fun hyber_get_message_history(period_in_seconds: Int): HyberFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("hyber_get_message_history period_in_seconds: $period_in_seconds")
            updateToken()
            PushKLoggerSdk.debug("Start hyber_get_message_history request: firebase_registration_token: ${PushKDatabase.firebase_registration_token}, hyber_registration_token: ${PushKDatabase.push_k_registration_token}, period_in_seconds: $period_in_seconds")
            if (PushKDatabase.registrationStatus) {
                val messHistPush: HyberFunAnswerGeneral = apiPushData.hGetMessageHistory(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token,
                    period_in_seconds
                )
                PushKLoggerSdk.debug("hyber_get_message_history mess_hist_hyber: $messHistPush")

                if (messHistPush.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(
                    messHistPush.code.toString(),
                    messHistPush.body,
                    "Success"
                )
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //4
    fun hyber_get_device_all_from_hyber(): HyberFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("Start hyber_get_device_all_from_hyber request: firebase_registration_token: ${PushKDatabase.firebase_registration_token}, hyber_registration_token: ${PushKDatabase.push_k_registration_token}")

            updateToken()
            if (PushKDatabase.registrationStatus) {
                val deviceAllPush: PushKDataApi = apiPushData.hGetDeviceAll(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                PushKLoggerSdk.debug("device_all_hyber : $deviceAllPush")

                if (deviceAllPush.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(
                    deviceAllPush.code.toString(),
                    deviceAllPush.body,
                    "Success"
                )
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //5
    fun hyber_update_registration(): HyberFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("hyber_update_registration started")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val resss: PushKDataApi = apiPushData.hDeviceUpdate(
                    PushKDatabase.push_k_registration_token,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    pushInternalParamsObject.push_k_deviceName,
                    pushDeviceType,
                    pushInternalParamsObject.push_k_osType,
                    pushInternalParamsObject.sdkVersion,
                    PushKDatabase.firebase_registration_token
                )
                if (resss.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(resss.code.toString(), resss.body, "Success")
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //6
    fun hyber_send_message_callback(
        message_id: String,
        message_text: String
    ): HyberFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("hyber_send_message_callback message_id: $message_id, message_text: $message_text")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val respp: PushKDataApi = apiPushData.hMessageCallback(
                    message_id,
                    message_text,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                if (respp.code == 401) {
                    try {
                        initHObject.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answerAny.generalAnswer(respp.code.toString(), respp.body, "Success")
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //7
    fun hyber_message_delivery_report(message_id: String): HyberFunAnswerGeneral {
        try {
            PushKLoggerSdk.debug("hyber_message_delivery_report message_id: $message_id")
            updateToken()
            if (PushKDatabase.registrationStatus) {
                if (PushKDatabase.push_k_registration_token != "" && PushKDatabase.firebase_registration_token != "") {
                    val respp1: PushKDataApi = apiPushData.hMessageDr(
                        message_id,
                        PushKDatabase.firebase_registration_token, //_xPushSessionId
                        PushKDatabase.push_k_registration_token
                    )
                    if (respp1.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answerAny.generalAnswer(respp1.code.toString(), respp1.body, "Success")
                } else {
                    return answerAny.generalAnswer(
                        "700",
                        "{}",
                        "Failed. firebase_registration_token or hyber_registration_token empty"
                    )
                }
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }


    //8 delete all devices
    fun hyber_clear_all_device(): HyberFunAnswerGeneral {
        try {
            updateToken()
            if (PushKDatabase.registrationStatus) {
                val deviceAllPush: PushKDataApi = apiPushData.hGetDeviceAll(
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )
                PushKLoggerSdk.debug("hyber_clear_all_device deviceAllPush: $deviceAllPush")

                val deviceList: String = parsing.parseIdDevicesAll(deviceAllPush.body)

                val pushAnswer: PushKDataApi = apiPushData.hDeviceRevoke(
                    deviceList,
                    PushKDatabase.firebase_registration_token, //_xPushSessionId
                    PushKDatabase.push_k_registration_token
                )

                PushKLoggerSdk.debug("hyber_answer : $pushAnswer")

                if (pushAnswer.code == 200) {
                    PushKLoggerSdk.debug("start clear data")
                    initHObject.clearData()
                    return answerAny.generalAnswer("200", "{\"devices\":$deviceList}", "Success")
                } else {
                    if (pushAnswer.code == 401) {
                        try {
                            initHObject.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answerAny.generalAnswer(
                        pushAnswer.code.toString(),
                        "{\"body\":\"unknown\"}",
                        "Some problem"
                    )

                }
            } else {
                return answerNotRegistered
            }

        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    //9temp
    fun rewrite_msisdn(newmsisdn: String): HyberFunAnswerGeneral {
        PushKLoggerSdk.debug("rewrite_msisdn start: $newmsisdn")
        return try {
            if (PushKDatabase.registrationStatus) {
                rewriteParams.rewritePushUserMsisdn(newmsisdn)
                answerAny.generalAnswer("200", "{}", "Success")
            } else {
                answerNotRegistered
            }
        } catch (e: Exception) {
            answerNotKnown
        }
    }

    //10temp
    fun rewrite_password(newPassword: String): HyberFunAnswerGeneral {

        PushKLoggerSdk.debug("rewrite_password start: $newPassword")

        return if (PushKDatabase.registrationStatus) {
            rewriteParams.rewritePushUserPassword(newPassword)
            answerAny.generalAnswer("200", "{}", "Success")
        } else {
            answerNotRegistered
        }
    }


    //11hyber
    fun hyber_check_queue(): HyberFunAnswerGeneral {
        try {
            updateToken()
            if (PushKDatabase.registrationStatus) {
                if (PushKDatabase.firebase_registration_token != "" && PushKDatabase.push_k_registration_token != "") {
                    val queue = QueueProc()
                    val answerData = queue.pushDeviceMessQueue(
                        PushKDatabase.firebase_registration_token,
                        PushKDatabase.push_k_registration_token, context
                    )

                    PushKLoggerSdk.debug("push_k_check_queue answerData: $answerData")

                    return answerAny.generalAnswer("200", "{}", "Success")
                } else {
                    return answerAny.generalAnswer(
                        "700",
                        "{}",
                        "Failed. firebase_registration_token or push_k_registration_token empty"
                    )
                }
            } else {
                return answerNotRegistered
            }
        } catch (e: Exception) {
            return answerNotKnown
        }
    }

    private fun updateToken() {
        PushKLoggerSdk.debug("PushSDK.updateToken started2")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    PushKLoggerSdk.debug("PushSDK.updateToken experimental: failed")
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result!!.token
                if (token != "") {
                    if (token != PushKDatabase.firebase_registration_token) {
                        PushKDatabase.firebase_registration_token = token
                        PushKLoggerSdk.debug("PushSDK.updateToken token2: $token")
                        rewriteParams.rewriteFirebaseToken(token)
                    }
                }
            })
        PushKLoggerSdk.debug("PushSDK.updateToken finished2")
    }
}
