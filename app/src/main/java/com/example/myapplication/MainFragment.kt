package com.example.myapplication

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.QRCode.R
import com.example.QRCode.databinding.FragmentMainBinding

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.*
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern


class MainFragment : Fragment() {
    private lateinit var binding:FragmentMainBinding
    private lateinit var retService: WordService
    private lateinit var mPermissions: Permissions
    private var mQRBitmap: Bitmap? = null
    var version: Int = 0
    private lateinit var updateTools: UpdateTools
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            GlobalScope.launch {
                mQRBitmap?.let { it1 -> savePhoto(it1) }
            }

        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMainBinding.inflate(layoutInflater)
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return binding.root
        //???fragment?????????oncreateOptionsMenu????????????onCrateView???????????????????????????????????????


    }

    //????????????????????????suspend???????????????????????????????????????????????????
    private suspend fun savePhoto(bitmap: Bitmap) {
        //?????????????????????
        withContext(Dispatchers.IO) {

//?????????????????????,??????????????????????????????????????????????????????????????????????????????????????????????????????????????????url??????????????????run??????
            val saveUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            ) ?: kotlin.run {
                MainScope().launch {
                    Toast.makeText(requireContext(), "????????????", Toast.LENGTH_SHORT).show()
                }
                return@withContext//????????????????????????
            }
            //???????????????????????????????????????uri???use????????????????????????io???
            requireContext().contentResolver.openOutputStream(saveUri).use {
                //?????????jpg??????????????????90???????????????it???io???
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) {//??????????????????ture
                    //toast??????????????????????????????????????????????????????MainScope().launch{??????}???????????????????????????
                    MainScope().launch {
                        Toast.makeText(
                            requireContext(),
                            "????????????",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(
                            requireContext(),
                            "????????????",
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
        responseLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
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
    }

    //????????????????????????
    fun recogQRcode(imageView: ImageView) {
        val QRbmp = (imageView.drawable as BitmapDrawable).bitmap //?????????bitmap???
        val width = QRbmp.width
        val height = QRbmp.height
        val data = IntArray(width * height)
        QRbmp.getPixels(data, 0, width, 0, 0, width, height) //????????????
        val source: Utility.RGBLuminanceSource = Utility.RGBLuminanceSource(QRbmp)
        // val source:com.google.zxing.RGBLuminanceSource=com.google.zxing.RGBLuminanceSource(QRbmp)
        //RGBLuminanceSource source = new RGBLuminanceSource(QRbmp);

//        val source: com.moli.qrcodetest7.MainActivity.RGBLuminanceSource =
//            com.moli.qrcodetest7.MainActivity.RGBLuminanceSource(QRbmp) //RGBLuminanceSource??????
        val bitmap1 = BinaryBitmap(HybridBinarizer(source))
        val reader = QRCodeReader()
        var re: Result? = null
        try {
            //????????????
            re = reader.decode(bitmap1)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
        //??????????????????????????????????????????URL???????????????????????????
//        val regex = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
//                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)") //?????????????????????
//        val pat = Pattern.compile(regex.trim { it <= ' ' }) //??????
//        val mat = pat.matcher(re!!.text.trim { it <= ' ' })
//        if (mat.matches()) {
            val uri = Uri.parse(re?.text)
            val intent = Intent(Intent.ACTION_VIEW, uri) //???????????????
            startActivity(intent)
      //  }

    }

    fun requestMultiplePermissions() {
        var permissions: Array<String> = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val register =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!) {// ??????
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

    private fun getVersion(version: Int) {
        val responseLiveData: LiveData<Response<Words>> = liveData {
            val response = retService.getWords()

            emit(response)
        }
        responseLiveData.observe(this, androidx.lifecycle.Observer {
            val words = it.body()
            val requestVersionCode= words?.data2?.get(0)?.AppVersion
            Log.e(TAG, "" + requestVersionCode)
            val requestDownloadUri=words?.data2?.get(0)?.DownloadUri
            Log.e(TAG, "" + requestDownloadUri)
            val requestDownloadPassword=words?.data2?.get(0)?.DownloadPassword
            Log.e(TAG, "" + requestDownloadPassword)
            val newversion: Double? = requestVersionCode?.toDouble()
            val oldversion = version.toDouble()
            if (newversion != oldversion) {
                if (newversion != null) {
                    if (newversion > oldversion) {
                        Log.e(TAG, "???????????????" + oldversion + "||" + "????????????" + newversion)
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("????????????")
                        builder.setMessage(R.string.DialogMessage)

                        builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
                            val uri = Uri.parse(requestDownloadUri)
                            val intent = Intent(Intent.ACTION_VIEW, uri) //???????????????
                            startActivity(intent)
                        }
                        builder.setNegativeButton(R.string.dialog_cannel) { dialog, which ->
                            Toast.makeText(requireContext(), "??????????????????", Toast.LENGTH_SHORT).show()
                           // Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show()
                        }
                        builder.show()
                    }
                }
            }
            else {

                Toast.makeText(requireContext(), "??????????????????", Toast.LENGTH_SHORT).show()

            }
        })

    }
}