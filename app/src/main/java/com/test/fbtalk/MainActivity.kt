package com.test.fbtalk

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dabada.downloader.PermissionUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    var imgUri: Uri? = null
    var nickName: String? = null
    var profileUrl: String? = null
    var isFirst = true
    var isChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PermissionUtil().isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionUtil().requestPermissions(this@MainActivity,1,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        var preferences = getSharedPreferences("account",MODE_PRIVATE)
        nickName = preferences.getString("nickName", "")!!
        profileUrl = preferences?.getString("profileUrl", null)

        if(nickName != null && nickName != ""){
            nickname.setText(nickName)
            Glide.with(this).load(profileUrl).into(profile)
            isFirst = false

            val intent = Intent(this@MainActivity, ChatActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_enter.setOnClickListener {
            if(!isChanged && !isFirst){
                var intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
                finish()
            }
            else saveUser()
        }

        profile.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            startActivityForResult(intent, 10)
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(object: OnCompleteListener<String>{
            override fun onComplete(task: Task<String>) {
                if(!task.isSuccessful()){
                    Log.w("FCM TOKEN ERROR", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                var token = task.getResult()
                val preferences = getSharedPreferences("account", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("token", token)
                editor.commit()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 10 && resultCode == RESULT_OK){
            imgUri = data?.data
            Glide.with(this).load(imgUri).into(profile)
            isChanged = true
        }
    }

    fun saveUser(){
        nickName = nickname.text.toString()
        if(nickName == "" || nickName == null){
            Toast.makeText(this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if(imgUri == null){
            Toast.makeText(this, "프로필 이미지를 선택해주세요.", Toast.LENGTH_LONG).show()
            return
        }
        val sdf = SimpleDateFormat("yyyyMMddhhmmss") //20191024111224
        val fileName: String = sdf.format(Date()).toString() + ".jpg"

        var firebaseStorage = FirebaseStorage.getInstance()
        var imgRef = firebaseStorage.getReference("profileImages/" + fileName)
        var uploadTask = imgRef.putFile(imgUri!!)

        uploadTask.continueWithTask {
            if(it.isSuccessful){
                it.exception?.let{
                    throw it
                }
            }
            imgRef.downloadUrl
        }.addOnCompleteListener {
            if(it.isSuccessful){
                var downloadUri = it.result
                profileUrl = downloadUri.toString()

                val preferences = getSharedPreferences("account", MODE_PRIVATE)
                var token = preferences.getString("token", null)
                var profileItem = Profile(nickName, profileUrl, token)

                var firebaseDatabase = FirebaseDatabase.getInstance()
                var profileRef = firebaseDatabase.getReference("profiles")
                profileRef.push().setValue(profileItem)

                val editor = preferences.edit()
                editor.putString("nickName", nickName)
                editor.putString("profileUrl", profileUrl)
                editor.commit()

                Toast.makeText(applicationContext, "프로필 저장 완료", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {
            it.printStackTrace()
            it.message
        }
    }
}