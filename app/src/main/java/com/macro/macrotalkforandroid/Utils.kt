package com.macro.macrotalkforandroid

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.LruCache
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.github.houbb.heaven.util.io.FileUtil.copyFile
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.GuideDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.macro.macrotalkforandroid.MainApplication.Companion.mainContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
                SettingData = SettingData(",", "#FFFFF9D9", true, 5, true)
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
            loadSetting()
        }

        fun saveSetting() {
            val saveFile = File("$appDataPath/Setting_Data.json")
            val gson = Gson()
            val jsonText = gson.toJson(SettingData, SettingData::class.java)
            saveFile.writeText(jsonText)
        }

        fun loadSetting() {
            val loadFile = File("$appDataPath/Setting_Data.json")
            if (!loadFile.exists()) return
            val gson = Gson()
            val data = gson.fromJson(loadFile.readText(), SettingData::class.java)
            SettingData = data
        }

        fun uriToFile(context: Context, uri: Uri): File? {
            var path = uri.path!!
            if (path.contains("%2F")) path = path.replace("%2F", "/")
            return when (uri.scheme) {
                ContentResolver.SCHEME_FILE -> {
                    val file = File(path)
                    val newfile = File(appDataPath + "/" + toMD5(file.name))
                    copyFile(file.absolutePath, newfile.absolutePath)
                    newfile
                }
                ContentResolver.SCHEME_CONTENT -> {
                    var path1 = PathUtils.getPath(context, uri)
                    if (path1!!.contains("%2F")) path1 = path1.replace("%2F", "/")
                    val file = File(path1!!)
                    val newfile = File(appDataPath + "/" + toMD5(file.name))
                    copyFile(file.absolutePath, newfile.absolutePath)
                    newfile
                }
                else -> null
            }
        }

        fun getVersionCode(mContext: Context): Int {
            var versionCode = 0
            try {
                //获取软件版本号，对应AndroidManifest.xml下android:versionCode
                versionCode =
                    mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return versionCode
        }

        fun shotRecyclerView(view: RecyclerView): Bitmap? {
            val adapter = view.adapter
            var bigBitmap: Bitmap? = null
            if (adapter != null) {
                val size = adapter.itemCount
                var height = 0
                val paint = Paint()
                var iHeight = 0
                val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

                // Use 1/8th of the available memory for this memory cache.
                val cacheSize = maxMemory / 8
                val bitmaCache: LruCache<String, Bitmap> = LruCache(cacheSize)
                for (i in 0 until size) {
                    val holder = adapter.createViewHolder(view, adapter.getItemViewType(i))
                    adapter.onBindViewHolder(holder, i)
                    holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    holder.itemView.layout(
                        0, 0, holder.itemView.measuredWidth,
                        holder.itemView.measuredHeight
                    )
                    holder.itemView.isDrawingCacheEnabled = true
                    holder.itemView.buildDrawingCache()
                    val drawingCache = holder.itemView.drawingCache
                    if (drawingCache != null) {
                        bitmaCache.put(i.toString(), drawingCache)
                    }
                    height += holder.itemView.measuredHeight
                }
                bigBitmap = Bitmap.createBitmap(view.measuredWidth, height, Bitmap.Config.ARGB_8888)
                val bigCanvas = Canvas(bigBitmap)
                val lBackground = view.background
                if (lBackground is ColorDrawable) {
                    val lColor = lBackground.color
                    bigCanvas.drawColor(lColor)
                }
                for (i in 0 until size) {
                    val bitmap: Bitmap = bitmaCache.get(i.toString())
                    bigCanvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
                    iHeight += bitmap.height
                    bitmap.recycle()
                }
            }
            return bigBitmap
        }

        fun saveImageToGallery(context: Context?, imageBytes: ByteArray?) {
            val albumPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
            val imageName = "${toMD5(imageBytes.toString())}.jpg"
            val imageFile = File(albumPath, imageName)
            try {
                val outputStream = FileOutputStream(imageFile)
                // 将图片内容写入文件
                outputStream.write(imageBytes)
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), null, null)
        }
    }
}

data class SettingData(
    var DefaultSplitChar : String,
    var ConversationBgColor : String,
    var AutoCollaspe : Boolean,
    var AutoCollaspeCount : Int,
    var HintDisplyed : Boolean
)

data class PrefabData(
    val Schools : List<School>,
    val Profiles : List<Profile>)

data class StorageData(
    val Schools : List<School>,
    var Profiles : List<Profile>,
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
) {
    fun toProfileSelector() : ProfileSelector {
        return if (Utils.prefabData.Profiles.contains(this)) {
            ProfileSelector(true, Utils.prefabData.Profiles.indexOf(this))
        } else {
            ProfileSelector(false, Utils.storageData.Profiles.indexOf(this))
        }
    }
}

data class Image(
    val ImageName : String,
    val ImageOriginalUri : String,
    val isNotPrefab : Boolean
) {
    fun toBitmap() : Bitmap {
        return if (!isNotPrefab) {
            val input = mainContext.assets.open("$ImageName.jpg")
            BitmapFactory.decodeStream(input)
        } else {
            BitmapFactory.decodeFile(ImageOriginalUri)
        }
    }
}

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