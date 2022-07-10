package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.CharacterSetECI
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*
import java.util.regex.Pattern

object Utility {
    const val WRITE_PERMISSION = 2134
    const val KEY_CONTENT = "KEY_CONTENT"
    const val REQUEST_CODE= 2304
    const val KEY_RESULT = "KEY_RESULT"
    @Throws(WriterException::class)
    fun generateQR(value: String) : Bitmap? {
        val bitMatrix: BitMatrix
        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.CHARACTER_SET] = CharacterSetECI.UTF8

            bitMatrix = MultiFormatWriter().encode(
                value,
                BarcodeFormat.QR_CODE,
                500,
                500,
                hints
            )
        }catch (Illegalargumentexception : IllegalArgumentException){
            return null
        }

        val bitMatrixWidth = bitMatrix.width

        val bitMatrixHeight = bitMatrix.height

        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth

            for (x in 0 until bitMatrixWidth) {

                pixels[offset + x] = if (bitMatrix.get(x, y))
                    Color.BLACK
                else
                    Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)

        return bitmap
    }

    //识别图片所需要的RGBLuminanceSource类
    class RGBLuminanceSource(bitmap: Bitmap) :
        LuminanceSource(bitmap.width, bitmap.height) {
        private val bitmapPixels: ByteArray
        override fun getMatrix(): ByteArray {
            // 返回我们生成好的像素数据
            return bitmapPixels
        }

        override fun getRow(y: Int, row: ByteArray): ByteArray {
            // 这里要得到指定行的像素数据
            System.arraycopy(bitmapPixels, y * width, row, 0, width)
            return row
        }

        init {

            // 首先，要取得该图片的像素数组内容
            val data = IntArray(bitmap.width * bitmap.height)
            bitmapPixels = ByteArray(bitmap.width * bitmap.height)
            bitmap.getPixels(data, 0, width, 0, 0, width, height)

            // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
            for (i in data.indices) {
                bitmapPixels[i] = data[i].toByte()
            }
        }
    }

}