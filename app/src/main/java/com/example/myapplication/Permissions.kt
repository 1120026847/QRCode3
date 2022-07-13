package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.QRCode.R

open class Permissions: AppCompatActivity() {
    var permissions: Permissions?=null
    /**
     * 申请多个权限
     */
    /**
     * 显示对话框
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        permissions=this
    }

    fun showReasonDialogSTORAGE(){

//        var msg =getString(
//            R.string.permission_dialog_message,
//            PermissionUtils.transform(
//                applicationContext,
//                permissions
//            )
//        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle("存储权限申请")
        builder.setMessage(R.string.permission_dialog_message_STORAGE)
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + applicationContext.getPackageName())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        }

        builder.setNegativeButton(R.string.dialog_cannel) { dialog, which ->
            Toast.makeText(this, "取消权限授予会导致保存图片的功能不可用", Toast.LENGTH_SHORT).show()
        }
        builder.show()

    }
}