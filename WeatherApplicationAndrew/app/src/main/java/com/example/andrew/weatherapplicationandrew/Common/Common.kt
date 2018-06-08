package com.example.andrew.weatherapplicationandrew.Common

import android.widget.EditText
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

object Common {
    val API_KEY = "4ec1989bc612d21c893ede9e87b864d8"

    val API_LINK = "https://api.openweathermap.org/data/2.5/weather"

    fun apiRequest(lat: String, lng: String): String {
        val sb = StringBuilder(API_LINK)
        sb.append("?lat=${lat}&lon=${lng}&APPID=${API_KEY}&units=imperial")

        return sb.toString()
    }
    fun apiZipRequest(txtCity: String): String {
      //  var mytxtZipCode.getText().toString()
        val sb = StringBuilder(API_LINK)
        sb.append("?zip=${txtCity},us&APPID=${API_KEY}&units=imperial")

        return sb.toString()
    }

    fun unixTimeStampToDateTime(unixTimeStamp:Int): String{
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong()*1000
        return dateFormat.format(date)
    }

    fun getImage(icon:String): String{
        return "http://openweathermap.org/img/w/${icon}.png "
    }

    val dateNow: String
        get(){
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val date = Date()
            return dateFormat.format(date)
        }



}