package com.Macro.macrotalkforandroid

import android.content.Context
import com.Macro.macrotalkforandroid.MainApplication.Companion.mainContext
import com.google.gson.Gson
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
                SettingData = SettingData(",", "0xFFFFF9D9")
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
    }
}

data class SettingData(
    val DefaultSplitChar : String,
    val ConversationBgColor : String
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
            val names = Profiles.map {
                if (it.isPrefab) {
                    Utils.prefabData.Profiles[it.PrefabIndex].Name
                } else {
                    Utils.storageData.Profiles[it.PrefabIndex].Name
                }
            }
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
)

data class Dialogue(
    val Type : DialogueType,
    val Name : String?,
    val Avator : Image?,
    val Content : Array<String>?,
    val ImageContent : Image?
)

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