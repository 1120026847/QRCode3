package com.example.myapplication

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.getVersion
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.myapplication.Utility.REQUEST_CODE
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.*
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern

const val TAG = "moli"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var retService: WordService
    private lateinit var mPermissions: Permissions
    private var mQRBitmap: Bitmap? = null
    var version: Int = 0
    private lateinit var updateTools: UpdateTools
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateTools = UpdateTools()
        mPermissions = Permissions()
        requestMultiplePermissions()
        retService = RetrofitInstance
            .getRetrofitInstance()
            .create(WordService::class.java)
        binding.ivGenerateRandom.setOnClickListener {
            getRamdomRequest()
        }
        binding.ivRandomQrcode.setOnLongClickListener {
            recogQRcode(binding.ivRandomQrcode)
            true
        }
        binding.ivQRcodeSave.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                    val strings = Array<String>(1) { "android.permission.WRITE_EXTERNAL_STORAGE" }
                    requestPermissions(strings, Utility.WRITE_PERMISSION)
                    return@setOnClickListener
                }
            }
            GlobalScope.launch {
                mQRBitmap?.let { it1 -> savePhoto(it1) }
            }
            //Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }
    }

    //存储图片的方法，suspend表示允许挂起，在另外一个线程上运行
    private suspend fun savePhoto(bitmap: Bitmap) {
        //声明运行的范围
        withContext(Dispatchers.IO) {

            //MediaStore是管理媒体的，bitmap就是要存储的对象，api为29之前可用
//        if (MediaStore.Images.Media.insertImage(requireContext().contentResolver,bitmap,"","")==null){
//            Toast.makeText(requireContext(),"存储失败",Toast.LENGTH_SHORT).show()
//        }else{
//            Toast.makeText(requireContext(),"存储成功",Toast.LENGTH_SHORT).show()
//        }

//设置保存的路径,第一个参数是设置存储的路径，第二个参数是设置存储时的参数，比如标题等；如果为url为空，就执行run方法
            val saveUri = this@MainActivity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            ) ?: kotlin.run {
                MainScope().launch {
                    Toast.makeText(this@MainActivity, "存储失败", Toast.LENGTH_SHORT).show()
                }
                return@withContext//程序停止向下运行
            }
            //第一个参数是写入流的目的地uri，use函数能够自动关闭io流
            this@MainActivity.contentResolver.openOutputStream(saveUri).use {
                //设置为jpg格式的图片，90的压缩率，it指io流
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) {//如果布尔值为ture
                    //toast只在主线程运行，不允许到父线程运行，MainScope().launch{代码}，就能解决这个问题
                    MainScope().launch {
                        Toast.makeText(
                            this@MainActivity,
                            "存储成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(
                            this@MainActivity,
                            "存储失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    }

    private fun getRamdomRequest() {
        val responseLiveData: LiveData<Response<Words>> = liveData {
            val response = retService.getWords()

            emit(response)
        }
        responseLiveData.observe(this, androidx.lifecycle.Observer {
            val words = it.body()
            val random = Random()
            val JsonArrayLength: Int? = words?.data?.size
            val RandomIndex = JsonArrayLength?.let { it1 -> random.nextInt(it1) }
            val requestRandomResult = RandomIndex?.let { it1 -> words.data.get(it1).word }
            // Toast.makeText(this, ""+requestRandomResult, Toast.LENGTH_SHORT).show()
            // Log.e(TAG, , )
            Log.e(TAG, "getRamdomRequest: " + requestRandomResult)
            mQRBitmap = requestRandomResult?.let { it1 -> Utility.generateQR(it1) }
            if (mQRBitmap != null) {
                // mTextInput = binding.GenerateWebEdittext.text.toString()
                binding.ivRandomQrcode
                    .setImageBitmap(mQRBitmap)
                //binding.ivGenerateWebSave.visibility = View.VISIBLE

            } else {
                //  binding.ivGenerateWebSave.visibility = View.INVISIBLE
                binding.ivRandomQrcode.setImageBitmap(null)
                mQRBitmap = null
            }
        })

        //        responseLiveData.observe(this, Observer {
//            val words = it.body()
//            val random= Random()
//            val JsonArrayLength: Int? =words?.data?.size
//            val RandomIndex= JsonArrayLength?.let { it1 -> random.nextInt(it1) }
//            /**
//            for (int i = 0; i < surveyListVO.getResult().size(); i++) {
//            System.out.print(surveyListVO.getResult().get(i)
//            .getSurveyId());  print: 12
//            System.out.print(surveyListVO.getResult().get(i)
//            .getSurveyName());   print: B///C
//            System.out.print(surveyListVO.getMessage());   print: success
//            }
//             */
////            Log.e("moli", ""+words )
////            Log.e("moli", ""+words?.data?.get(0) )
////            Log.e("moli", ""+words?.data?.get(0)?.id )
////            Log.e("moli", ""+words?.data?.get(0)?.newsId)
////            Log.e("moli", ""+words?.msg)
//            //   Log.e("moli", ""+words?.data?.get(RandomIndex)?.word )
//
//            // Log.e("moli", ""+randomtext )
//            textView.setText(""+ RandomIndex?.let { it1 -> words?.data?.get(it1)?.word })
////            if (words != null) {
////
////            }
//        })
//    }

    }

    //识别二维码的函数
    fun recogQRcode(imageView: ImageView) {
        val QRbmp = (imageView.drawable as BitmapDrawable).bitmap //将图片bitmap化
        val width = QRbmp.width
        val height = QRbmp.height
        val data = IntArray(width * height)
        QRbmp.getPixels(data, 0, width, 0, 0, width, height) //得到像素
        val source: Utility.RGBLuminanceSource = Utility.RGBLuminanceSource(QRbmp)
        // val source:com.google.zxing.RGBLuminanceSource=com.google.zxing.RGBLuminanceSource(QRbmp)
        //RGBLuminanceSource source = new RGBLuminanceSource(QRbmp);

//        val source: com.moli.qrcodetest7.MainActivity.RGBLuminanceSource =
//            com.moli.qrcodetest7.MainActivity.RGBLuminanceSource(QRbmp) //RGBLuminanceSource对象
        val bitmap1 = BinaryBitmap(HybridBinarizer(source))
        val reader = QRCodeReader()
        var re: Result? = null
        try {
            //得到结果
            re = reader.decode(bitmap1)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
//        val editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
//        //向SharedPreferences.Editor对象中添加数据
//        editor.putString("QRCode_WEB",re!!.text)
//        //调用apply()方法将添加的数据提交，从而完成数据存储操作
//        editor.apply()
//        val intent=Intent(this,ResultActivity::class.java)
//        startActivity(intent)
//        //Toast出内容
//        //  Toast.makeText(this@MainActivity, re!!.text, Toast.LENGTH_SHORT).show()
//        Log.e(TAG, re!!.text, )
        //利用正则表达式判断内容是否是URL，是的话则打开网页
        val regex = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)") //设置正则表达式
        val pat = Pattern.compile(regex.trim { it <= ' ' }) //比对
        val mat = pat.matcher(re!!.text.trim { it <= ' ' })
        if (mat.matches()) {
            val uri = Uri.parse(re?.text)
            val intent = Intent(Intent.ACTION_VIEW, uri) //打开浏览器
            startActivity(intent)
        }

    }

    fun requestMultiplePermissions() {
        var permissions: Array<String> = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val register =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!) {// 同意
                } else {
                    mPermissions.showReasonDialogSTORAGE()
                }

                if (it[Manifest.permission.READ_EXTERNAL_STORAGE]!!) {
                } else {
                    mPermissions.showReasonDialogSTORAGE()
                }
            }
        register.launch(permissions)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        R.id.update->
            getVersion(updateTools.getVersion(this));
R.id.aboutAuthor->
    AboutAuthorStart()
        }
        return true
    }

    private fun getVersion(version: Int) {
        val responseLiveData: LiveData<Response<Words>> = liveData {
            val response = retService.getWords()

            emit(response)
        }
        responseLiveData.observe(this, androidx.lifecycle.Observer {
            val words = it.body()
         //   val random = Random()
           // val JsonArrayLength: Int? = words?.data?.size
           // val RandomIndex = JsonArrayLength?.let { it1 -> random.nextInt(it1) }
            val requestVersionCode= words?.data2?.get(0)?.AppVersion
            Log.e(TAG, ""+requestVersionCode, )
            val requestDownloadUri=words?.data2?.get(0)?.DownloadUri
            Log.e(TAG, ""+requestDownloadUri, )
            val requestDownloadPassword=words?.data2?.get(0)?.DownloadPassword
            Log.e(TAG, ""+requestDownloadPassword, )
            val newversion: Double? = requestVersionCode?.toDouble()
            val oldversion = version.toDouble()
            if (newversion != oldversion) {
                if (newversion != null) {
                    if (newversion > oldversion) {
                        Log.e(TAG, "旧版本为：" + oldversion + "||" + "新版本为" + newversion)
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("是否更新")
                        builder.setMessage(R.string.DialogMessage)
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                        builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
                            val uri = Uri.parse(requestDownloadUri)
                            val intent = Intent(Intent.ACTION_VIEW, uri) //打开浏览器
                            startActivity(intent)
                        }
                        builder.setNegativeButton(R.string.dialog_cannel) { dialog, which ->
                            Toast.makeText(this, "你已取消更新", Toast.LENGTH_SHORT).show()
                        }
                        builder.show()
                    }
                }
            }
            else {

                Toast.makeText(this, "已是最新版本", Toast.LENGTH_SHORT).show()

            }
           // val requestRandomResult = RandomIndex?.let { it1 -> words.data.get(it1).word }
            // Toast.makeText(this, ""+requestRandomResult, Toast.LENGTH_SHORT).show()
            // Log.e(TAG, , )
//            Log.e(TAG, "getRamdomRequest: " + requestRandomResult)
//            mQRBitmap = requestRandomResult?.let { it1 -> Utility.generateQR(it1) }
//            if (mQRBitmap != null) {
//                // mTextInput = binding.GenerateWebEdittext.text.toString()
//                binding.ivRandomQrcode
//                    .setImageBitmap(mQRBitmap)
//                //binding.ivGenerateWebSave.visibility = View.VISIBLE
//
//            } else {
//                //  binding.ivGenerateWebSave.visibility = View.INVISIBLE
//                binding.ivRandomQrcode.setImageBitmap(null)
//                mQRBitmap = null
//            }
        })
        //——————————————————————————————————————————————————



//        val data = ""
//        val newversion = "2.1"
//        Log.e(TAG, "newversion=" + newversion)
//        val content = "\n" +
//                "就不告诉你我们更新了什么-。-\n" +
//                "\n" +
//                "----------万能的分割线-----------\n" +
//                "\n" +
//                "(ㄒoㄒ) 被老板打了一顿，还是来告诉你吧：\n" +
//
//                "1.下架商品误买了？恩。。。我搞了点小动作就不会出现了\n" +
//                "2.侧边栏、弹框优化 —— 这个你自己去探索吧，总得留点悬念嘛-。-\n";//更新内容
//        val url = ""
//        val newversioncode: Double = newversion.toDouble()
//        Log.e(TAG, "新版本为" + newversioncode)
//        val oldversion = version.toDouble()
//        //val cc=newversioncode.toInt()
//        Log.e(TAG, "旧版本为：" + oldversion + "||" + "新版本为" + newversioncode)
//        if (newversioncode != oldversion) {
//            if (newversioncode > oldversion) {
//                Log.e(TAG, "旧版本为：" + oldversion + "||" + "新版本为" + newversioncode)
//                ShowUpdateDialog()
//            }
//
//
//        }
    }

    fun ShowUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("是否更新")
        builder.setMessage(R.string.DialogMessage)
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
            val uri = Uri.parse("https://www.csdn.net/")
            val intent = Intent(Intent.ACTION_VIEW, uri) //打开浏览器
            startActivity(intent)
        }

        builder.setNegativeButton(R.string.dialog_cannel) { dialog, which ->
            Toast.makeText(this, "你已取消更新", Toast.LENGTH_SHORT).show()
        }
        builder.show()

    }
    fun AboutAuthorStart(){
        val intent =Intent(this,AboutAuthor::class.java)
        startActivity(intent)
    }
}

