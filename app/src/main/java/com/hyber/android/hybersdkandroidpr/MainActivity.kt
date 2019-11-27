package com.hyber.android.hybersdkandroidpr

import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.iid.FirebaseInstanceId
import android.util.Log
import java.io.IOException
import android.content.ContentValues.TAG
import android.content.Intent
import android.annotation.SuppressLint
import android.app.Notification
import android.widget.Button
import android.widget.EditText

import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ActivityCompat
import android.telephony.TelephonyManager
import android.content.Context
import android.text.TextUtils
import android.os.Build
import android.content.BroadcastReceiver
import android.content.IntentFilter

import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import kotlin.properties.Delegates
import android.hardware.fingerprint.FingerprintManager


import com.hyber.android.hybersdkandroid.core.HyberFunAnswerRegister
import com.hyber.android.hybersdkandroid.core.HyberFunAnswerGeneral
import com.hyber.android.hybersdkandroid.HyberMessaging
import com.hyber.android.hybersdkandroid.HyberPushMess

import android.os.Bundle
import com.google.android.gms.tasks.OnSuccessListener
import com.hyber.android.hybersdkandroid.HyberSDK


class ForegroundBackgroundListener(textBox: EditText) : LifecycleObserver {


    //any classes initialization
    private var textBox: EditText by Delegates.notNull()

    //main class initialization
    init {
        this.textBox = textBox
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startSomething() {
        Log.v("ProcessLog", "APP IS ON FOREGROUND")
        println("foreground")


        if (HyberPushMess.message != null) {
            val mess: String = textBox.text.toString()
            textBox.setText(mess + "\n" + HyberPushMess.message)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopSomething() {
        Log.v("ProcessLog", "APP IS IN BACKGROUND")
        println("background")
    }
}


class MainActivity : AppCompatActivity() {

    var branch_h = "master"


    val mPlugInReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (HyberPushMess.message != null) {
                val edtName: EditText = findViewById(R.id.editText2)
                val mess: String = edtName.text.toString()
                edtName.setText(mess + "\n" + HyberPushMess.message)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction("com.hyber.android.hybersdkandroid.Push")
        //val receiver: MyReceiver = MyReceiver()
        registerReceiver(mPlugInReceiver, filter)
        //val ddd:HyberSDK = HyberSDK("375291234567", "password",this)
        //ddd.hyber_check_queue()
    }

    override fun onResume() {
        super.onResume()
        val ddd:HyberSDK = HyberSDK("375291234567", "password",this)
        ddd.hyber_check_queue()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mPlugInReceiver)
        //val ddd:HyberSDK = HyberSDK("375291234567", "password",this)
        //ddd.hyber_check_queue()
    }






    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var intentService: Intent = Intent()
        if (Build.VERSION.SDK_INT<=25) {
            intentService = Intent(this, HyberMessaging::class.java)
            startService(intentService);
        }

        val permissions = arrayOf(android.Manifest.permission.READ_PHONE_STATE)
        ActivityCompat.requestPermissions(this, permissions,0)

        //val permissions2 = arrayOf(android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
        //ActivityCompat.requestPermissions(this, permissions2,0)



        val button1: Button = findViewById(R.id.button)
        val edtName: EditText = findViewById(R.id.editText2)
        val edittext1: EditText = findViewById(R.id.editText) as EditText
        edtName.setSelection(0)

        button1.setOnClickListener {
            val text_msisdn:String = edtName.text.toString()
            val registration:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)

            val msisd:String =edittext1.text.toString()
            println(msisd)
            registration.rewrite_msisdn(msisd)

            registration.rewrite_branch(    branch_h)


            val registration_test: HyberFunAnswerRegister = registration.hyber_register_new("test","AIzaSyDvNUnk7R5Qx_aaMCFjFAWTi2jY8vbZW88")
            edtName.setText(text_msisdn + "\n" + registration_test.toString())
        }




        val ddd123:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(
                ForegroundBackgroundListener(edtName)
                //.also { appObserver = it }
            )


        val instanceId = FirebaseInstanceId.getInstance().instanceId
        println("instanceId fb: $instanceId")
        val token = FirebaseInstanceId.getInstance().token
        println("Token fb: $token")






        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)
        val button4: Button = findViewById(R.id.button4)
        val button5: Button = findViewById(R.id.button5)
        val button6: Button = findViewById(R.id.button6)
        //message_callback
        val button7: Button = findViewById(R.id.button7)
        //delivery_report
        val button8: Button = findViewById(R.id.button8)
        //init
        val button9: Button = findViewById(R.id.button9)
        val button12: Button = findViewById(R.id.button12)





        button12.setOnClickListener {
            //val total = intent.getStringExtra("test")
            //edtName.setText(total)

            //val token222 = FirebaseInstanceId.getInstance().token.toString()
            //println("token new: $token222")


            var token111: String = String()
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
                token111 = instanceIdResult.token
                // Do whatever you want with your token now
                // i.e. store it on SharedPreferences or DB
                // or directly send it to server
                println("ggg: $token111")
            }

            println(token111)

           // var fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
           // edtName.setText(fingerprintManager.toString())
           // println(fingerprintManager)
            //setResult(RESULT_OK, intent);
        }



        button2.setOnClickListener {
            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)
            val sssss2: HyberFunAnswerGeneral = ddd.hyber_clear_current_device()
            edtName.setText(tttt + "\n" + sssss2.toString())
        }

        //get phone data
        button3.setOnClickListener {

            val phoneMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var fgfd = phoneMgr.line1Number
            //println(asdfasd.hyber_uuid)
            var dsdf = edittext1.text.toString()
            var RELEASE: String = Build.VERSION.RELEASE
            var SDK_INT: Int = Build.VERSION.SDK_INT
            val afasd: String = Build.VERSION.BASE_OS
            val osbase: String = Build.VERSION.CODENAME
            val OS = System.getProperty("os.name")!!
            println(OS)
            val serial = Build.SERIAL
            val tppp = Build.TYPE
            val fingerprint = Build.FINGERPRINT
            val board = Build.BOARD
            val phoneMgr2 = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var ffff = phoneMgr2.phoneType
            var sdfsd:String = String()
            if (ffff== TelephonyManager.PHONE_TYPE_NONE)
            {
                sdfsd = "Tablet"
            } else {
                sdfsd = "Phone"
            }

            edtName.setText("Msisdn: $fgfd" +
                    "\n" +
                    "Imei " + "\n" +
                    "release: $RELEASE" + "\n" +
                    "SDK_INT: $SDK_INT" + "\n" +
                    "baseos: $afasd" + "\n" +
                    "osbase: $osbase" + "\n" +
                    "os: $OS"  + "\n" +
                    "serial: $serial"  + "\n" +
                    "tppp: $tppp"  + "\n" +
                    "fingerprint: $fingerprint"  + "\n" +
                    "board: $board"  + "\n" +
                    "ptype: $sdfsd"
            )
        }

        button4.setOnClickListener {

            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            val msisd:String =edittext1.text.toString()
            println(msisd)
            ddd.rewrite_msisdn(msisd)
            ddd.rewrite_branch(    branch_h)
            val sssss4: HyberFunAnswerGeneral = ddd.hyber_get_message_history(7200)
            edtName.setText(tttt + "\n" + sssss4)

        }

        button5.setOnClickListener {

            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)

            val sssss5: HyberFunAnswerGeneral = ddd.hyber_get_device_all_from_hyber()

            edtName.setText(tttt + "\n" + sssss5)

        }

        button6.setOnClickListener {

            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)

            val sssss5: HyberFunAnswerGeneral = ddd.hyber_update_registration()

            edtName.setText(tttt + "\n" + sssss5)

        }

        button7.setOnClickListener {

            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)
            val sssss7: HyberFunAnswerGeneral = ddd.hyber_send_message_callback("23f2f2f2f2f","Hello World")
            edtName.setText(tttt + "\n" + sssss7)
        }

        button8.setOnClickListener {
            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)
            val sssss8: HyberFunAnswerGeneral = ddd.hyber_message_delivery_report("23f2f2f2f2f")

            edtName.setText(tttt + "\n" + sssss8)

        }

        button9.setOnClickListener {

            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)

        }


        //clear all
        button10.setOnClickListener {

            val tttt:String = edtName.text.toString()
            val ddd:HyberSDK = HyberSDK(edittext1.text.toString(), "password",this)
            ddd.rewrite_msisdn(edittext1.text.toString())
            ddd.rewrite_branch(    branch_h)
            val sssss33: HyberFunAnswerGeneral = ddd.hyber_clear_all_device()
            edtName.setText(tttt + "\n" + sssss33)

        }

        button11.setOnClickListener {
            edtName.setText("")
        }
    }


}
