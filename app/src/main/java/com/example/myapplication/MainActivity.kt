package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cn.jpush.android.api.JPushInterface
import com.example.QRCode.R
import com.example.QRCode.databinding.ActivityMainBinding

import com.google.android.material.bottomnavigation.BottomNavigationView

const val TAG = "moli"

class MainActivity : AppCompatActivity() {
    //for receive customer msg from jpush server
    private var mMessageReceiver: MessageReceiver ?= null
    val MESSAGE_RECEIVED_ACTION="com.example.myapplication.MESSAGE_RECEIVED_ACTION"
    // val MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION"
    val KEY_TITLE = "title"
    val KEY_MESSAGE = "message"
    val KEY_EXTRAS = "extras"
    private val msgText: TextView? = null
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        JPushInterface.init(application)//极光接口初始化，否则用不了
        registerMessageReceiver();//注册消息接收器
        //判断该app是否打开了通知，如果没有的话就打开手机设置页面
        if (!isNotificationEnabled(this)) {
            //开启通知弹窗
            gotoSet()
        } else {

        }
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_main, R.id.navigation_home
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun registerMessageReceiver() {
        mMessageReceiver = MessageReceiver()
        val filter: IntentFilter = IntentFilter()
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        filter.addAction(MESSAGE_RECEIVED_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver!!, filter)
    }
    //todo
    inner class MessageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent?.action)) {
                    val message: String? = intent?.getStringExtra(KEY_MESSAGE)
                    val extras: String? = intent?.getStringExtra(KEY_EXTRAS)
                    val showMsg: StringBuilder = StringBuilder()
                    showMsg.append(KEY_MESSAGE + " : " + message + "\n")
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n")
                    }
                    setCostomMsg(showMsg.toString());
                }
            } catch (e: java.lang.Exception) {
            }
        }
    }
//    fun MessageReceiver():BroadcastReceiver {
//        object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (MESSAGE_RECEIVED_ACTION.equals(intent?.action)) {
//                    val message: String? = intent?.getStringExtra(KEY_MESSAGE)
//                    val extras: String? = intent?.getStringExtra(KEY_EXTRAS)
//                    val showMsg: StringBuilder = StringBuilder()
//                    showMsg.append(KEY_MESSAGE + " : " + message + "\n")
//                    if (!ExampleUtil.isEmpty(extras)) {
//                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n")
//                    }
//                    setCostomMsg(showMsg.toString());
//                }
//            }
//
//        }
//    }

    //    fun MessageReceiver()=object :BroadcastReceiver(){
//        override fun onReceive(context: Context?, intent: Intent?) {
//         if (MESSAGE_RECEIVED_ACTION.equals(intent?.action)){
//             val message: String? =intent?.getStringExtra(KEY_MESSAGE)
//             val extras:String?=intent?.getStringExtra(KEY_EXTRAS)
//             val showMsg:StringBuilder= StringBuilder()
//             showMsg.append(KEY_MESSAGE + " : " + message + "\n")
//             if (!ExampleUtil.isEmpty(extras)){
//                 showMsg.append(KEY_EXTRAS + " : " + extras + "\n")
//             }
//             setCostomMsg(showMsg.toString());
//         }
//        }
//    }
    fun setCostomMsg(msg:String){
        if (null != msgText){
            msgText.text=msg
            msgText.visibility= View.VISIBLE

        }
    }
    fun isNotificationEnabled(context: Context):Boolean{
        var isOpened:Boolean=false
        try {
            isOpened= NotificationManagerCompat.from(context).areNotificationsEnabled()
        }catch (e:Exception){
            e.printStackTrace();
            isOpened = false;
        }
        return isOpened
    }
    fun gotoSet(){
        val  intent: Intent = Intent()
        if (Build.VERSION.SDK_INT>=26){
            intent.action="android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName())
        }else if(Build.VERSION.SDK_INT>=21){
            intent.action="android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", getPackageName())
            intent.putExtra("app_uid", getApplicationInfo().uid)
        }else{
            intent.action="android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.setData(Uri.fromParts("package", getPackageName(), null))
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

