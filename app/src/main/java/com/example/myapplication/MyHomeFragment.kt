package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.QRCode.R
import com.example.QRCode.databinding.FragmentMyHomeBinding

import retrofit2.Response

class MyHomeFragment : Fragment() {
private lateinit var binding:FragmentMyHomeBinding
    private lateinit var updateTools: UpdateTools
    private lateinit var retService: WordService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTools = UpdateTools()
        retService = RetrofitInstance
            .getRetrofitInstance()
            .create(WordService::class.java)
        binding.checkUpdate.setOnClickListener {
            getVersion(updateTools.getVersion(requireContext()));
        }
        binding.feedBack.setOnClickListener {
            StartFeedBack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMyHomeBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun getVersion(version: Int) {
        val responseLiveData: LiveData<Response<Words>> = liveData {
            val response = retService.getWords()

            emit(response)
        }
        responseLiveData.observe(requireActivity(), androidx.lifecycle.Observer {
            val words = it.body()
            //   val random = Random()
            // val JsonArrayLength: Int? = words?.data?.size
            // val RandomIndex = JsonArrayLength?.let { it1 -> random.nextInt(it1) }
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
                        Log.e(TAG, "旧版本为：" + oldversion + "||" + "新版本为" + newversion)
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("是否更新")
                        builder.setMessage(R.string.DialogMessage)
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                        builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
                            val uri = Uri.parse(requestDownloadUri)
                            val intent = Intent(Intent.ACTION_VIEW, uri) //打开浏览器
                            startActivity(intent)
                        }
                        builder.setNegativeButton(R.string.dialog_cannel) { dialog, which ->
                            Toast.makeText(requireContext(), "你已取消更新", Toast.LENGTH_SHORT).show()
                            // Toast.makeText(this, "你已取消更新", Toast.LENGTH_SHORT).show()
                        }
                        builder.show()
                    }  else {
                        Toast.makeText(requireContext(), "已是最新版本", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
        //——————————————————————————————————————————————————

    }
fun  StartFeedBack(){
    val intent=Intent(requireContext(),FeedBackActivity::class.java)
    startActivity(intent)
}
}