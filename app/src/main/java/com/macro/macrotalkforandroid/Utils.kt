package com.macro.macrotalkforandroid

import android.content.Context
import com.macro.macrotalkforandroid.MainApplication.Companion.mainContext
import com.google.gson.Gson
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

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
                storageData = StorageData(listOf(), listOf(), listOf<Conversation>().toMutableList())
            }

        var SettingData : SettingData
            init {
                SettingData = SettingData(",", "#FFFFF9D9", true, 5)
            }

        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        fun toMD5(str : String) : String {
            val md = MessageDigest.getInstance("MD5")
            md.update(str.toByteArray())
            val hashedPwd = BigInteger(1, md.digest()).toString(16)
            return hashedPwd
        }

        fun save() {
            val saveFile = File("$appDataPath/Save_Data.json")
            val gson = Gson()
            val jsonText = gson.toJson(storageData, StorageData::class.java)
            saveFile.writeText(jsonText)
        }

        fun load() {
            val loadFile = File("$appDataPath/Save_Data.json")
            if (!loadFile.exists()) return
            val gson = Gson()
            val data = gson.fromJson(loadFile.readText(), StorageData::class.java)
            storageData = data
        }
    }
}

data class SettingData(
    val DefaultSplitChar : String,
    val ConversationBgColor : String,
    val AutoCollaspe : Boolean,
    val AutoCollaspeCount : Int
)

data class PrefabData(
    val Schools : List<School>,
    val Profiles : List<Profile>)

data class StorageData(
    val Schools : List<School>,
    val Profiles : List<Profile>,
    val Conversations : MutableList<Conversation>)

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
    val ImageOriginalUri : String,
    val isNotPrefab : Boolean
)

data class Birthday(
    val Month : Int,
    val Day : Int
)

data class Conversation(
    val Title : String,
    val Profiles : List<ProfileSelector>,
    val Image : Image,
    val Tags : List<String>?
) {
    val LastDialogue : String
        get() {
            if (Dialogues.size == 0) return ""
            val dialogue = Dialogues[Dialogues.size - 1]
            val names = Profiles.map { it.toProfile().Name }
            if (names.size == 1) {
                return when (dialogue.Type) {
                    DialogueType.Knot -> "[羁绊剧情]"
                    DialogueType.Reply -> "[回复]"
                    DialogueType.ImageStudent1 -> "[图片]"
                    DialogueType.ImageStudent2 -> "[图片]"
                    DialogueType.ImageTeacher -> "[图片]"
                    else -> dialogue.Content!![0]
                }
            } else {
                return when (dialogue.Type) {
                    DialogueType.Knot -> "[羁绊剧情]"
                    DialogueType.Reply -> "[回复]"
                    DialogueType.ImageStudent1 -> dialogue.Name + ": [图片]"
                    DialogueType.ImageStudent2 -> dialogue.Name + ": [图片]"
                    DialogueType.ImageTeacher -> dialogue.Name + ": [图片]"
                    else -> dialogue.Name + ": " + dialogue.Content!![0]
                }
            }
        }
    val Dialogues : MutableList<Dialogue> = listOf<Dialogue>().toMutableList()
}

data class ProfileSelector(
    val isPrefab : Boolean,
    val PrefabIndex : Int
) {
    fun toProfile() : Profile {
        return if (isPrefab) {
            Utils.prefabData.Profiles[PrefabIndex]
        } else {
            Utils.storageData.Profiles[PrefabIndex]
        }
    }
}

data class Dialogue(
    val Type : DialogueType?,
    val Name : String?,
    val Avator : Image?,
    val Content : List<String>?,
    val ImageContent : Image?,
    val AvatorOverwrite : Image?,
    val NameOverwrite : String?
) {
    companion object {
        val Empty : Dialogue = Dialogue(null, null, null, null, null, null, null)
    }
}

enum class DialogueType {
    Student1,
    Student2,
    Teacher,
    Narrator,
    Knot,
    Reply,
    ImageStudent1,
    ImageStudent2,
    ImageTeacher
}