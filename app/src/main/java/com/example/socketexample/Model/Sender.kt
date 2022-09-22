package com.example.socketexample.Model

 class Sender{
    var id:String? = ""
     lateinit var name: String
     constructor()
    constructor ( id:String, name:String){
        this.id = id
        this.name = name
    }
 }
