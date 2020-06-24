package jp.techacademy.satoshi.autoslideshowapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private val PERMISSION_REQUEST_CODE = 100

    lateinit var resolver:ContentResolver
    var cursor:Cursor? = null
    var imageUri:Uri? = null
    var ContentsMax = 0
    var ContentsPosition = 0

    private var mTimer: Timer? = null
    private var mTimerSec = 0.0
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null  // ソート (null ソートなし)
        )

        nextButton.setOnClickListener(this)
        playButton.setOnClickListener(this)
        backButton.setOnClickListener(this)

        // ANDROID 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        cursor!!.moveToFirst()
        // indexからIDを取得し、そのIDから画像のURIを取得する
        var fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        var id = cursor!!.getLong(fieldIndex)
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                ContentsMax++
            } while (cursor!!.moveToNext())
        }
        cursor!!.moveToFirst()
        //cursor!!.close()
    }

    private fun nextContentsInfo() {
        ContentsPosition++

        if (ContentsPosition == ContentsMax) {
            cursor!!.moveToFirst()
            ContentsPosition = 0
        } else {
            cursor!!.moveToNext()
        }
        // indexからIDを取得し、そのIDから画像のURIを取得する
        var fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        var id = cursor!!.getLong(fieldIndex)
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)

        //cursor!!.close()
    }

    private fun backContentsInfo() {
        if (ContentsPosition == 0) {
            cursor!!.moveToLast()
            ContentsPosition = ContentsMax
        } else {
            cursor!!.moveToPrevious()
        }
        ContentsPosition--
        // indexからIDを取得し、そのIDから画像のURIを取得する
        var fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        var id = cursor!!.getLong(fieldIndex)
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)

        //cursor!!.close()
    }

    private fun playContentsInfo() {
        if (mTimer == null){
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mTimerSec += 0.1
                    mHandler.post {
                        nextContentsInfo()
                    }
                }
            }, 2000, 2000) // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定
            playButton.text = "停止"
            nextButton.isClickable = false
            backButton.isClickable = false
        } else {
            if (mTimer != null){
                mTimer!!.cancel()
                mTimer = null
            }
            playButton.text = "再生"
            nextButton.isClickable = true
            backButton.isClickable = true
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.nextButton -> nextContentsInfo()
            R.id.playButton -> playContentsInfo()
            R.id.backButton -> backContentsInfo()
        }
    }

}
