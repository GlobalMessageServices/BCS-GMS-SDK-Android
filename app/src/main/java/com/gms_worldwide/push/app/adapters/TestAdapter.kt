package com.gms_worldwide.push.app.adapters

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gms_worldwide.push.app.R
import com.push.android.pushsdkandroid.PushSDK
import kotlinx.android.synthetic.main.test_item.view.*


class TestAdapter(private val pushSDK: PushSDK) :
    RecyclerView.Adapter<TestAdapter.TestViewHolder>() {


    class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.test_item,
                parent,
                false
            )
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {

        holder.itemView.apply {

            api_response.movementMethod = ScrollingMovementMethod()
            api_response.setOnClickListener {
                val cm: ClipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.text = api_response.text
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }

            api_response.setOnTouchListener { v, event ->
                api_response.parent.requestDisallowInterceptTouchEvent(true)
                false
            }

            devices.setOnClickListener {
                //api_response.movementMethod = ScrollingMovementMethod()
                api_response.text = pushSDK.getAllRegisteredDevices().toString()
            }
            updateBtn.setOnClickListener {
                api_response.movementMethod = ScrollingMovementMethod()
                api_response.text = pushSDK.updateRegistration().toString()
            }

            clear_device.setOnClickListener {
                api_response.movementMethod = ScrollingMovementMethod()
                api_response.text = pushSDK.unregisterCurrentDevice().toString()
            }
        }

    }

    override fun getItemCount(): Int {
        return 1
    }
}