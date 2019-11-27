package com.hyber.android.hybersdkandroid

import android.app.Activity
import android.content.Context
import com.hyber.android.hybersdkandroid.add.Answer
import android.util.Log
import android.content.ContentValues.TAG
import java.lang.Exception
import com.hyber.android.hybersdkandroid.add.GetInfo
import com.hyber.android.hybersdkandroid.add.RewriteParams
import com.hyber.android.hybersdkandroid.add.HyberParsing
import com.hyber.android.hybersdkandroid.core.*
import com.hyber.android.hybersdkandroid.core.Initialization
import com.hyber.android.hybersdkandroid.core.HyberApi
import com.hyber.android.hybersdkandroid.core.HyberDataApi
import kotlin.properties.Delegates

object HyberPushMess {
    var message: String? = null   //global variable
}

class HyberSDKQueue() {
    fun hyber_check_queue(context: Context): HyberFunAnswerGeneral {
        val answer_not_known: HyberFunAnswerGeneral =
            HyberFunAnswerGeneral(710, "Failed", "Unknown error", "unknown")

        try {
            val answ: Answer = Answer()
            val answer_not_registered: HyberFunAnswerGeneral = HyberFunAnswerGeneral(
                704,
                "Failed",
                "Registration data not found",
                "Not registered"
            )

            val inithyber_params2: Initialization = Initialization(context)
            inithyber_params2.hyber_init3()
            if (inithyber_params2.paramsglobal.registrationstatus == true) {
                val queue: QueueProc = QueueProc()
                val anss = queue.hyber_device_mess_queue(
                    inithyber_params2.paramsglobal.firebase_registration_token,
                    inithyber_params2.paramsglobal.hyber_registration_token, context
                )
                println(anss)
                return answ.general_answer("200", "{}", "Success")
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            //println("failed rewrite password")
            return answer_not_known
        }
    }
}

class HyberSDK(
    user_msisdn: String = "unknown",
    user_password: String = "unknown",
    context: Context
) {

    //any classes initialization
    private var context: Context by Delegates.notNull()
    private var init_hyber: Initialization = Initialization(context)
    private var local_device_info: GetInfo = GetInfo()
    private var user_msisdn: String by Delegates.notNull()
    private var user_password: String by Delegates.notNull()
    private var apihyber: HyberApi = HyberApi()
    private var answ: Answer = Answer()
    private var rewrite_params: RewriteParams = RewriteParams(context)
    private var parsing: HyberParsing = HyberParsing()

    //main class initialization
    init {
        this.context = context
        this.user_msisdn = user_msisdn
        this.user_password = user_password
        init_hyber.hyber_init(
            "android",
            user_msisdn,
            user_password,
            local_device_info.get_phone_type(context),
            local_device_info.getDeviceName().toString()
        )
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                this.hyber_update_registration()
            }
        } catch (e: Exception) {
            println("registration update problem")
        }
    }

    //private var hyber_storage: HyberStorage =HyberStorage(context)
    //parameter for device identification
    private var X_Hyber_Session_Id: String = init_hyber.paramsglobal.firebase_registration_token

    private var answer_not_registered: HyberFunAnswerGeneral =
        HyberFunAnswerGeneral(704, "Failed", "Registration data not found", "Not registered")
    private var answer_not_known: HyberFunAnswerGeneral =
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

    //{
    //    "result":"Ok",
    //    "description":"",
    //    "code":200,
    //    "body":{
    //}
    //}


    //1
    private fun initsdk(
        hyber_osType: String,
        hyber_user_msisdn: String,
        hyber_user_Password: String,
        hyber_deviceType: String
    ) {
        init_hyber.hyber_init(
            "android",
            hyber_user_msisdn,
            hyber_user_Password,
            hyber_deviceType,
            local_device_info.getDeviceName().toString()
        )
    }

    //1
    fun hyber_register_new(
        X_Hyber_Client_API_Key: String,
        X_Hyber_App_Fingerprint: String
    ): HyberFunAnswerRegister {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                return answ.hyber_register_new_register_exists2(init_hyber.paramsglobal, context)
            } else {
                val answ_hyber: HyberDataApi2 = apihyber.hyber_device_register(
                    X_Hyber_Client_API_Key,
                    X_Hyber_Session_Id,
                    X_Hyber_App_Fingerprint,
                    init_hyber.paramsglobal.hyber_deviceName,
                    init_hyber.paramsglobal.hyber_deviceType,
                    init_hyber.paramsglobal.hyber_osType,
                    init_hyber.paramsglobal.sdkversion,
                    init_hyber.paramsglobal.hyber_user_Password,
                    init_hyber.paramsglobal.hyber_user_msisdn,
                    context
                )
                Log.d(TAG, "hyber_register_new response: $answ_hyber");
                Log.d(TAG, "uuid: ${init_hyber.paramsglobal.hyber_uuid}");
                return HyberFunAnswerRegister(
                    code = answ_hyber.code,
                    result = answ_hyber.body.result,
                    description = answ_hyber.body.description,
                    deviceId = answ_hyber.body.deviceId,
                    token = answ_hyber.body.token,
                    userId = answ_hyber.body.userId,
                    userPhone = answ_hyber.body.userPhone,
                    createdAt = answ_hyber.body.createdAt
                )
            }
        } catch (e: Exception) {
            return answ.register_procedure_answer2("700", "unknown", context)
        }
        //apihyber.hyber_device_register()
    }

    //2
    fun hyber_clear_current_device(): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                val hyber_answer: HyberDataApi = apihyber.hyber_device_revoke(
                    "[\"${init_hyber.paramsglobal.deviceId}\"]",
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_registration_token
                )
                Log.d(TAG, "hyber_answer : ${hyber_answer.toString()}");

                if (hyber_answer.code == 200) {
                    Log.d(TAG, "start clear data");
                    init_hyber.clearData()
                    return answ.general_answer(
                        "200",
                        "{\"device\":\"${init_hyber.paramsglobal.deviceId}\"}",
                        "Success"
                    )
                } else {
                    if (hyber_answer.code == 401) {
                        try {
                            init_hyber.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answ.general_answer(
                        hyber_answer.code.toString(),
                        "{\"body\":\"unknown\"}",
                        "Some problem"
                    )
                }
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            return answer_not_known
        }
    }

    //return all message history till time
    //3
    fun hyber_get_message_history(period_in_seconds: Int): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                val mess_hist_hyber: HyberFunAnswerGeneral = apihyber.hyber_get_message_history(
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_registration_token,
                    period_in_seconds
                )
                println(mess_hist_hyber)
                if (mess_hist_hyber.code == 401) {
                    try {
                        init_hyber.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answ.general_answer(
                    mess_hist_hyber.code.toString(),
                    mess_hist_hyber.body,
                    "Success"
                )
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            return answer_not_known
        }
    }

    //4
    fun hyber_get_device_all_from_hyber(): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                val device_all_hyber: HyberDataApi = apihyber.hyber_get_device_all(
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_registration_token
                )
                Log.d(TAG, "device_all_hyber : $device_all_hyber");
                if (device_all_hyber.code == 401) {
                    try {
                        init_hyber.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answ.general_answer(
                    device_all_hyber.code.toString(),
                    device_all_hyber.body,
                    "Success"
                )
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            return answer_not_known
        }
    }

    private fun init_firebase(context: Context, activity: Activity, icon: Int) {
        //val fireb: MyFirebaseInstanceIdService = MyFirebaseInstanceIdService(context, activity, icon)
        //FirebaseApp.initializeApp(context)
        println("Token firebase: ${init_hyber.paramsglobal.firebase_registration_token}")
    }

    //5
    fun hyber_update_registration(): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                val resss: HyberDataApi = apihyber.hyber_device_update(
                    init_hyber.paramsglobal.hyber_registration_token,
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_deviceName,
                    init_hyber.paramsglobal.hyber_deviceType,
                    init_hyber.paramsglobal.hyber_osType,
                    init_hyber.paramsglobal.sdkversion,
                    init_hyber.paramsglobal.firebase_registration_token
                )
                if (resss.code == 401) {
                    try {
                        init_hyber.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answ.general_answer(resss.code.toString(), resss.body, "Success")
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            return answer_not_known
        }
    }

    //6
    fun hyber_send_message_callback(
        message_id: String,
        message_text: String
    ): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                val respp: HyberDataApi = apihyber.hyber_message_callback(
                    message_id,
                    message_text,
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_registration_token
                )
                if (respp.code == 401) {
                    try {
                        init_hyber.clearData()
                    } catch (ee: Exception) {
                    }
                }
                return answ.general_answer(respp.code.toString(), respp.body, "Success")
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            return answer_not_known
        }
    }

    //7
    fun hyber_message_delivery_report(message_id: String): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                if (init_hyber.paramsglobal.hyber_registration_token != "" && X_Hyber_Session_Id != "") {
                    val respp1: HyberDataApi = apihyber.hyber_message_dr(
                        message_id,
                        X_Hyber_Session_Id,
                        init_hyber.paramsglobal.hyber_registration_token
                    )
                    if (respp1.code == 401) {
                        try {
                            init_hyber.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answ.general_answer(respp1.code.toString(), respp1.body, "Success")
                } else {
                    return answ.general_answer(
                        "700",
                        "{}",
                        "Failed. firebase_registration_token or hyber_registration_token empty"
                    )
                }
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            return answer_not_known
        }
    }


    //8 delete all devices
    fun hyber_clear_all_device(): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                val device_all_hyber: HyberDataApi = apihyber.hyber_get_device_all(
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_registration_token
                )

                val device_list: String = parsing.parse_id_devices_all(device_all_hyber.body)

                val hyber_answer: HyberDataApi = apihyber.hyber_device_revoke(
                    device_list,
                    X_Hyber_Session_Id,
                    init_hyber.paramsglobal.hyber_registration_token
                )
                Log.d(TAG, "hyber_answer : $hyber_answer");

                if (hyber_answer.code == 200) {
                    Log.d(TAG, "start clear data");
                    init_hyber.clearData()
                    return answ.general_answer("200", "{\"devices\":$device_list}", "Success")
                } else {
                    if (hyber_answer.code == 401) {
                        try {
                            init_hyber.clearData()
                        } catch (ee: Exception) {
                        }
                    }
                    return answ.general_answer(
                        hyber_answer.code.toString(),
                        "{\"body\":\"unknown\"}",
                        "Some problem"
                    )

                }
            } else {
                return answer_not_registered
            }

        } catch (e: Exception) {
            return answer_not_known
        }
    }

    //9temp
    fun rewrite_msisdn(newmsisdn: String): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                rewrite_params.rewrite_hyber_user_msisdn(newmsisdn)
                return answ.general_answer("200", "{}", "Success")
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            //println("failed rewrite msisdn")
            return answer_not_known
        }
    }

    //10temp
    fun rewrite_password(newpassword: String): HyberFunAnswerGeneral {
        try {
            if (init_hyber.paramsglobal.registrationstatus == true) {
                rewrite_params.rewrite_hyber_user_password(newpassword)
                return answ.general_answer("200", "{}", "Success")
            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            //println("failed rewrite password")
            return answer_not_known
        }
    }


    //11hyber
    fun hyber_check_queue(): HyberFunAnswerGeneral {
        try {
            val inithyber_params2: Initialization = Initialization(context)
            inithyber_params2.hyber_init3()
            if (inithyber_params2.paramsglobal.registrationstatus == true) {
                if (inithyber_params2.paramsglobal.firebase_registration_token != "" && inithyber_params2.paramsglobal.hyber_registration_token != "") {
                    val queue: QueueProc = QueueProc()
                    val anss = queue.hyber_device_mess_queue(
                        inithyber_params2.paramsglobal.firebase_registration_token,
                        inithyber_params2.paramsglobal.hyber_registration_token, context
                    )
                    println(anss)
                    return answ.general_answer("200", "{}", "Success")
                } else {
                    return answ.general_answer(
                        "700",
                        "{}",
                        "Failed. firebase_registration_token or hyber_registration_token empty"
                    )
                }

            } else {
                return answer_not_registered
            }
        } catch (e: Exception) {
            //println("failed rewrite password")
            return answer_not_known
        }
    }

    //12
    fun rewrite_branch(hyber_branch: String): HyberFunAnswerGeneral {
        try {
            rewrite_params.rewrite_hyber_branch(hyber_branch)
            return answ.general_answer("200", "{}", "Success")
        } catch (e: Exception) {
            //println("failed rewrite password")
            return answer_not_known
        }
    }

}

