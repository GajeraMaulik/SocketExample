package com.example.socketexample.Model

data class Message(

    val text:String,
    val receiver_id:String,
    val sender_id:String,
    val sender:Sender,
    val receiver:Receiver,
    val group_id: String,
    val group: Group?,
    val time:String,
    val photo:String,
    val gender:Boolean
)
