package com.example.socketexample
import android.content.Context
import android.content.SharedPreferences
import android.util.Log.d
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class SharePref {

    companion object {
        private const val MySharedPref = "MySharedPref"

        fun save(context: Context, key: String, value: String) {
            val sharedPref = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun save(context: Context, key: String, value: Int) {
            val sharedPref = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        fun save(context: Context, key: String, value: Boolean) {
            val sharedPref = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun save(context: Context, key: String, value: Long) {
            val sharedPref = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putLong(key, value)
            editor.apply()
        }


        fun save(context: Context, key: String?, value: Float) {
            val sharedPreferences = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putFloat(key, value)
            editor.apply()
        }

        fun saveObject(context: Context,key: String?,value: JSONObject){
            val sharedPreferences = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(value)
            editor.putString(key,json)
            editor.commit()
            d("---->jsondata","----->$json")
        }



        fun saveValue(context: Context, key: String?, value: Int) {
            val sharedPreferences = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
          //  editor.putStringSet(key,value)
            editor.apply()
        }

     /*   fun saveArrayList(context: Context, value: ArrayList<OrderInfo>, key: String?) {
            val  sharedPreferences = context.getSharedPreferences(MySharedPref,Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json: String = gson.toJson(value)
            editor.putString(key,json)
            editor.apply()
        }


        fun getArrayList(context: Context,key: String): ArrayList<OrderInfo> {
            val  sharedPreferences = context.getSharedPreferences(MySharedPref,Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String = sharedPreferences.getStringSet(key, null).toString()
            val type: Type = object : TypeToken<ArrayList<OrderInfo>>() {}.getType()
            return gson.fromJson(json, type)
        }
*/

 /*       fun getJsonObject(context: Context,key: String): String? {
            val sharedPreferences = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val json = sharedPreferences.getString(key,"")
            val gson = Gson()
       //    val data = gson.fromJson(json,key:)
        //    return  data
        }*/


        fun getIntValue(context: Context, key: String?): Int {
            return context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE).getInt(key, -1)
        }

        fun getBooleanValue(context: Context, key: String?): Boolean {
            return context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
                .getBoolean(key, false)
        }

        fun getStringValue(context: Context, key: String): String? {
            return context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
                .getString(key, " ")
        }


        fun getLongValue(context: Context, key: String?): Long {
            return context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE).getLong(key, 0)
        }

        private fun saveMap(context: Context, key: String, inputMap: Map<String, Int>) {
            val pSharedPref: SharedPreferences =
                context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val jsonObject = JSONObject(inputMap)
            val jsonString: String = jsonObject.toString()
            val editor = pSharedPref.edit()
            editor.remove(key).apply()
            editor.putString(key, jsonString)
            editor.apply()
        }

        private fun loadMap(context: Context, key: String): Map<String, Int> {
            val outputMap: MutableMap<String, Int> = HashMap()
            val pSharedPref: SharedPreferences =
                context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            try {
                val jsonString = pSharedPref.getString(key, JSONObject().toString())
                val jsonObject = JSONObject(jsonString!!)
                val keysItr: Iterator<String> = jsonObject.keys()
                while (keysItr.hasNext()) {
                    val k = keysItr.next()
                    val v = jsonObject.get(k) as Int
                    outputMap[k] = v
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return outputMap
        }


        fun removeSharePref(context: Context) {
            val sharedPreferences = context.getSharedPreferences(MySharedPref, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }

        fun logout(context: Context,username: String?) {
            val preferences :SharedPreferences  = context.getSharedPreferences (MySharedPref, Context.MODE_PRIVATE);
            val editor  = preferences.edit()
            editor.remove(username)
            editor.clear()
            editor.apply()

        }


        fun toast(context: Context, msg: String) {
            Toast.makeText(
                context,
                msg, Toast.LENGTH_SHORT
            ).show()
        }

        fun greeting(): String {
            var displayGreeting = ""
            val c = Calendar.getInstance()
            when (c[Calendar.HOUR_OF_DAY]) {
                in 0..11 -> {
                    displayGreeting = "Good Morning"
                }
                in 12..15 -> {
                    displayGreeting = "Good Afternoon"
                }
                in 16..20 -> {
                    displayGreeting = "Good Evening"
                }
                in 21..23 -> {
                    displayGreeting = "Good Night"
                }
            }
            return displayGreeting
        }
    }
}

private fun SharedPreferences.Editor.putStringSet(key: String?, json: String) {

}






