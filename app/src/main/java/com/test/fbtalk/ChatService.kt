package com.test.fbtalk

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chat.*

class ChatService: JobIntentService() {

    companion object {
        const val NOTIFY_ID = 0x472
        const val JOB_ID = 1001
    }

    fun enqueueWork(context:Context, work: Intent){
        enqueueWork(context, ChatService::class.java, JOB_ID, work)
    }

    override fun onHandleWork(intent: Intent) {
        var nickname = intent.getStringExtra("nickname")
        var firebasesDatabase = FirebaseDatabase.getInstance()
        var chatRef = firebasesDatabase.getReference("chat")
        chatRef.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var messageItem = snapshot.getValue(Message::class.java)
                if(nickname != messageItem?.nickname) {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channelId = "FBTalk"
                    val notificationBuilder: NotificationCompat.Builder
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(channelId, "FBTalk", NotificationManager.IMPORTANCE_LOW)
                        channel.enableVibration(true)
                        notificationManager.createNotificationChannel(channel)
                        notificationBuilder = NotificationCompat.Builder(
                                this@ChatService, channel.id)
                    } else {
                        notificationBuilder = NotificationCompat.Builder(this@ChatService, channelId)
                    }
                    val pendingIntent = PendingIntent.getActivity(applicationContext, 0,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    notificationBuilder.setSmallIcon(R.drawable.pic)
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageItem?.nickname)
                            .setContentText(messageItem?.message)
                            .setAutoCancel(true)
                    notificationManager.notify(NOTIFY_ID, notificationBuilder.build())
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}