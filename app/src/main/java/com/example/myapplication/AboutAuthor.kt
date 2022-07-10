package com.example.myapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapplication.databinding.ActivityAboutAuthorBinding
const val key = "ih3iF7F6oq_0fzkxkhjVEkmeoTTPpAlz"
class AboutAuthor : AppCompatActivity() {
    private lateinit var binding:ActivityAboutAuthorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAboutAuthorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        binding.ConnectQQ.setOnClickListener {
            try {
                //第二种方式：可以跳转到添加好友，如果qq号是好友了，直接聊天
                val url = "mqqwpa://im/chat?chat_type=wpa&uin=" + 1120026847;//uin是发送过去的qq号码
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (e: Exception) {
                e.printStackTrace();
                Toast.makeText(this, "请检查是否安装QQ", Toast.LENGTH_SHORT).show()
            }
        }
        binding.ConnectGroup.setOnClickListener {
            val intent = Intent();
            intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
            // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent);

            } catch (e:Exception) {
                // 未安装手Q或安装的版本不支持
                Toast.makeText(this, "请检查是否安装QQ", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
//            val intent=Intent(this,MainActivity::class.java)
//            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}