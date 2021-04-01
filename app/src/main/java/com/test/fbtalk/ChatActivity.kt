package com.test.fbtalk

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.view.*
import kotlinx.android.synthetic.main.item_chat.view.*
import java.util.*

var myNick = ""

class ChatActivity: AppCompatActivity() {
    lateinit var chatRef: DatabaseReference
    lateinit var nickName: String
    lateinit var profileUrl: String
    var messageItems = ArrayList<Message>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var preferences = getSharedPreferences("account",MODE_PRIVATE)
        nickName = preferences.getString("nickName", "")!!
        profileUrl = preferences?.getString("profileUrl", "")!!
        myNick = nickName

//        var intent = Intent(this, ChatService::class.java)
//        intent.putExtra("nickname", nickName)
//        startService(intent)
//        ChatService().enqueueWork(this,intent)

        rv_chat.layoutManager = LinearLayoutManager(this)
        rv_chat.adapter = ChatAdapter(arrayListOf())

        var firebasesDatabase = FirebaseDatabase.getInstance()
        chatRef = firebasesDatabase.getReference("chat")
        chatRef.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var newMessageItems = ArrayList<Message>()
                var messageItem = snapshot.getValue(Message::class.java)
                var oldCount = messageItems.size
                messageItems.add(messageItem!!)
                newMessageItems.add(messageItem)
                var adapter = rv_chat.adapter as ChatAdapter
                adapter.items.addAll(newMessageItems)
                adapter.notifyItemRangeChanged(oldCount+1, 1)
                adapter.notifyDataSetChanged()
                rv_chat.scrollToPosition(messageItems.size -1)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        btn_send.setOnClickListener {
            clickSend()
        }

    }

    fun clickSend(){
        var message = edit_message.text.toString()
        var calendar = Calendar.getInstance()

        var minute = ""
        if(calendar[Calendar.MINUTE] < 10) minute = "0" + calendar[Calendar.MINUTE]
        else minute = ""+calendar[Calendar.MINUTE]
        val time = calendar[Calendar.HOUR_OF_DAY].toString() + ":" + minute
        val messageItem = Message(nickName, message, time, profileUrl)
        chatRef.push().setValue(messageItem)
        edit_message.setText("")
    }

    class ChatAdapter(val items: ArrayList<Message>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType){
                0 -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
                else -> OtherViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_chat,parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ViewHolder) {
                holder.bind(items[position])
            } else if (holder is OtherViewHolder){
                holder.bind(items[position])
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when(items[position].nickname){
                myNick -> 0
                else -> 1
            }
        }

        override fun getItemCount(): Int {
            return items.count()
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: Message) {
                itemView.item_message.text = item.message
                itemView.item_nickname.text = item.nickname
                itemView.item_time.text = item.time
                Glide.with(itemView.context).load(item.profileUrl).into(itemView.item_profile)
                itemView.item_message.setOnClickListener(object: View.OnClickListener{
                    override fun onClick(p0: View?) {
                        
                    }
                })
            }
        }

        class OtherViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bind(item: Message){
                itemView.item_message.text = item.message
                itemView.item_nickname.text = item.nickname
                itemView.item_time.text = item.time
                Glide.with(itemView.context).load(item.profileUrl).into(itemView.item_profile)
            }
        }
    }
}