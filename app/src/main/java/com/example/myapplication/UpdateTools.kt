package com.example.myapplication

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

open class UpdateTools{
     fun getVersion(context: Context):Int{
        val manager:PackageManager=context.packageManager
        val info:PackageInfo=manager.getPackageInfo(context.packageName,0)
      //  val version:String=info.versionName;
        val versioncode:Int=info.versionCode
       return  versioncode
    }

}