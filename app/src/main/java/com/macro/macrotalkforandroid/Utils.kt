package com.macro.macrotalkforandroid

import android.content.Context
import com.macro.macrotalkforandroid.MainApplication.Companion.mainContext
import com.google.gson.Gson
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

class Utils {
    companion object {
        // 应用程序数据保存路径
        var appDataPath = mainContext.getExternalFilesDir(null).toString()

        // 预制数据
        var prefabData: PrefabData
        init {
            // 从 assets 中读取预制数据
            val stream = mainContext.assets.open("Prefab_Macro Talk_Data.json")
            val fileLength = stream.available()
            val buffer = ByteArray(fileLength)
            stream.read(buffer)
            stream.close()
            val jsonText = String(buffer, charset("utf-8"))
            val data = Gson().fromJson(jsonText, PrefabData::class.java)
            prefabData = data
        }

        // 存储数据
        var storageData: StorageData
        init {
            storageData = StorageData(listOf(), listOf(), mutableListOf())
        }

        // 设置数据
        var SettingData: SettingData
        init {
            SettingData = SettingData(",", "#FFFFF9D9", true, 5)
        }

        // 将 dp 转换为 px
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        // 将字符串转换为 MD5
        fun toMD5(str: String): String {
            val md = MessageDigest.getInstance("MD5")
            md.update(str.toByteArray())
            val hashedPwd = BigInteger(1, md.digest()).toString(16)
            return hashedPwd
        }

        // 保存数据到文件
        fun save() {
            val saveFile = File("$appDataPath/Save_Data.json")
            val gson = Gson()
            val jsonText = gson.toJson(storageData, StorageData::class.java)
            saveFile.writeText(jsonText)
        }

        // 从文件加载数据
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
    // 默认分隔字符
    val DefaultSplitChar: String,
    // 对话背景颜色
    val ConversationBgColor: String,
    // 是否自动折叠对话
    val AutoCollaspe: Boolean,
    // 自动折叠的对话数量
    val AutoCollaspeCount: Int
)

data class PrefabData(
    // 学校列表
    val Schools: List<School>,
    // 预制档案列表
    val Profiles: List<Profile>
)

data class StorageData(
    // 学校列表
    val Schools: List<School>,
    // 自定义档案列表
    val Profiles: List<Profile>,
    // 会话列表
    val Conversations: MutableList<Conversation>
)

data class School(
    // 学校名称
    val SchoolName: String,
    // 学校图标路径
    val SchoolIconUri: String
)

data class Profile(
    // 名称
    val Name: String,
    // 姓氏
    val FirstName: String?,
    // 图片列表
    val Images: List<Image>,
    // 年龄
    val Age: Int?,
    // 身高
    val Height: Int?,
    // 生日
    val BirthDay: Birthday?,
    // 学校
    val School: String?,
    // 爱好
    val Hobbies: List<String>?,
    // Momotalk 状态
    val MomotalkState: String?,
    // 描述
    val Description: String?,
    // 标签
    val Tags: List<String>?
)

data class Image(
    // 图片名称
    val ImageName: String,
    // 图片原始路径
    val ImageOriginalUri: String,
    // 是否为非预制的标记
    val isNotPrefab: Boolean
)

data class Birthday(
    // 月份
    val Month: Int,
    // 日
    val Day: Int
)

data class Conversation(
    // 标题
    val Title: String,
    // 档案选择器列表
    val Profiles: List<ProfileSelector>,
    // 图片
    val Image: Image,
    // 标签列表
    val Tags: List<String>?
) {
    // 最后对话内容
    val LastDialogue: String
        get() {
            if (Dialogues.size == 0) return ""
            val dialogue = Dialogues[Dialogues.size - 1]
            val names = Profiles.map { it.toProfile().Name }
            if (names.size == 1) {
                return when (dialogue.Type) {
                    // 羁绊剧情
                    DialogueType.Knot -> "[羁绊剧情]"
                    // 回复
                    DialogueType.Reply -> "[回复]"
                    // 图片
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
    // 对话列表
    val Dialogues: MutableList<Dialogue> = listOf<Dialogue>().toMutableList()
}

data class ProfileSelector(
    // 是否为预制档案
    val isPrefab: Boolean,
    // 预制档案索引
    val PrefabIndex: Int
) {
    // 转换为档案
    fun toProfile(): Profile {
        return if (isPrefab) {
            Utils.prefabData.Profiles[PrefabIndex]
        } else {
            Utils.storageData.Profiles[PrefabIndex]
        }
    }
}

data class Dialogue(
    // 对话类型
    val Type: DialogueType?,
    // 名称
    val Name: String?,
    // 头像
    val Avator: Image?,
    // 内容
    val Content: List<String>?,
    // 图片内容
    val ImageContent: Image?,
    // 头像重写
    val AvatorOverwrite: Image?,
    // 名称重写
    val NameOverwrite: String?
) {
    companion object {
        // 空对话
        val Empty: Dialogue = Dialogue(null, null, null, null, null, null, null)
    }
}

// 对话类型枚举
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
