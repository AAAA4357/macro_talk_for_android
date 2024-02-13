package com.example.macrotalkforandroid

import android.R
import android.icu.text.Collator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.macrotalkforandroid.MainApplication.Companion.mainContext
import com.google.gson.Gson
import java.io.UnsupportedEncodingException


class Utils {
    companion object {
        var appDataPath = mainContext.getExternalFilesDir(null).toString()

        var prefabData : PrefabData
            init {
                val stream = mainContext.assets.open("Prefab_Macro Talk_Data.json")
                val fileLength = stream.available()
                val buffer = ByteArray(fileLength)
                stream.read(buffer)
                stream.close()
                val jsonText = String(buffer, charset("utf-8"))
                val data = Gson().fromJson(jsonText, PrefabData::class.java)
                prefabData = data
            }

        var storageData : StorageData
            init {
                storageData = StorageData(listOf(), listOf(), listOf())
            }
    }
}

data class PrefabData(
    val Schools : List<School>,
    val Profiles : List<Profile>)

data class StorageData(
    val Schools : List<School>,
    val Profiles : List<Profile>,
    val Conversations : List<Conversation>)

data class School(
    val SchoolName : String,
    val SchoolIconUri : String
)

data class Profile(
    val Name : String,
    val FirstName : String?,
    val Images : List<Image>,
    val Age : Int?,
    val Height : Int?,
    val BirthDay : Birthday?,
    val School : String?,
    val Hobbies : List<String>?,
    val MomotalkState : String?,
    val Description : String?,
    val Tags : List<String>?
)

data class Image(
    val ImageName : String,
    val ImageOriginalUri : String
)

data class Birthday(
    val Month : Int,
    val Day : Int
)

data class Conversation(
    val Title : String,

)