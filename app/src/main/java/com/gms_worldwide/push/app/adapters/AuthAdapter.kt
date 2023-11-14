package com.gms_worldwide.push.app.adapters

import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gms_worldwide.push.app.MainActivity
import com.gms_worldwide.push.app.R
import com.gms_worldwide.push.app.models.Message
import com.gms_worldwide.push.app.models.PushAnswerRegister
import com.gms_worldwide.push.app.service.Service
import com.push.android.pushsdkandroid.PushSDK
import com.push.android.pushsdkandroid.models.PushKFunAnswerGeneral
import com.push.android.pushsdkandroid.models.PushKFunAnswerRegister
import com.push.android.pushsdkandroid.models.PushSDKRegAnswerResult

import kotlinx.android.synthetic.main.auth_item.view.*

/**
 * AuthAdapter is controller for 'auth_item' layout.
 * @param mainActivity
 * @param mainAdapter controller of main page.
 * @param viewer RecyclerView of main activity layout.
 * @param pushSDK instance of PushSDK from Android SDK.
 * @param service Message and PushAnswerRegister service.
 */

class AuthAdapter(
    private var mainActivity: MainActivity,
    private var mainAdapter: MainAdapter,
    private var viewer: RecyclerView,
    private var pushSDK: PushSDK,
    private var service: Service
) : RecyclerView.Adapter<AuthAdapter.AuthViewHolder>() {

    class AuthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Called when ViewHolder creates.
     * @return instance of inner class AuthViewHolder. Contains 'auth_item' layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthViewHolder {
        return AuthViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.auth_item,
                parent,
                false
            )
        )
    }

    /**
     * Called when ViewHolder binds.
     * Description of 'auth_item' layout behavior.
     */
    override fun onBindViewHolder(holder: AuthViewHolder, position: Int) {
        holder.itemView.apply {
            btnAddNumber.setOnClickListener {


                var phoneNumber = number.text

                if (phoneNumber.isNullOrEmpty() || phoneNumber.toString().length < 10
                    || phoneNumber.toString().length > 13 || phoneNumber.toString()
                        .contains("+") || !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber.toString())
                ) {
                    number.error = "only numbers are allowed, length = 10-13"

                } else {

                        if (mainActivity.isDeviceRegistered()) {
                            var unregisterResponse = pushSDK.unregisterCurrentDevice()
                            if (unregisterResponse.code == 200) {
                                //service.deleteAllRegisterData()
                                var response = registerNumber(phoneNumber.toString())

                                if (!checkRegistrationResponse(response)) {
                                    number.error =
                                        "$response - error was occurred during number registration"
                                }
                            } else {
                                number.error =
                                    "$unregisterResponse - error was occurred during number registration"
                            }
                        } else {
                            var response = registerNumber(phoneNumber.toString())

                            if (!checkRegistrationResponse(response)) {
                                number.error =
                                    "$response - error was occurred during number registration"
                            }
                        }

                }


            }

        }
    }


    /**
     * Get item count. Returns needed count of 'auth_item' layout.
     * In this case, returns 1, as we need only one item of 'auth_item' layout.
     * @return Int.
     */
    override fun getItemCount(): Int {
        return 1
    }


    /**
     * Register phone number with current device.
     * @param number phone number.
     * @return PushKFunAnswerRegister.
     */
    private fun registerNumber(number: String): PushKFunAnswerRegister {

        var response: PushKFunAnswerRegister = pushSDK.registerNewDevice(
            "client-api-key",  //API key that you would be provided with
            "app-finger-print", //APP fingerprint that you would be provided with
            number, //Device's phone number
            "Android" //password, associated with Device's phone number (legacy - it is unused, you can put any value)
        )
        var updateResponse : PushKFunAnswerGeneral
        if (response.code == 200){
            updateResponse = pushSDK.updateRegistration()
        }

        Log.d("TAG", response.toString())



        return response


        /*val answer2 = pushSDK.updateRegistration()
        Log.d("TAG", answer2.toString())*/
    }


    /**
     * Check if registration was successful.
     * @param response PushKFunAnswerRegister.
     * @return Boolean.
     */
    private fun checkRegistrationResponse(response: PushKFunAnswerRegister): Boolean {
        if (response.code == 200 || (response.code == 701 && response.result == PushSDKRegAnswerResult.EXISTS)) {


            //save registration date in local DB
/*            var answer = PushAnswerRegister(
                response.deviceId,
                response.token,
                response.userId,
                response.userPhone,
                response.createdAt
            )
            service.saveRegisterDataInDB(answer)*/

            mainAdapter.messages = emptyArray<Message>().toMutableList()
            viewer.adapter = mainAdapter
            viewer.layoutManager = LinearLayoutManager(mainActivity)

            return true
        } else {
            return false
        }
    }
}