package com.macro.macrotalkforandroid.ui.profilelist

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.macro.macrotalkforandroid.Birthday
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Calendar

// 自定义对话框视图绑定类
class AddProfileBindView(val isRewrite: Boolean, val resources: Resources, val context: Context, val tab: ProfileTab) :
    OnBindView<CustomDialog>(
        R.layout.fragment_addprofile
    ) {
    // 学生头像列表
    var avatorList: List<String> = listOf()
    // 学生生日
    var birthday = Birthday(1, 1)
    // 选择的索引
    var index = -1
    lateinit var view: View

    override fun onBind(dialog: CustomDialog?, v: View?) {
        view = v!!
        // 设置输入框的提示文本
        val hobbies = v.findViewById<EditText>(R.id.add_profile_hobbies)
        hobbies.hint = resources.getString(R.string.add_profile_hobbies, Utils.SettingData.DefaultSplitChar)
        val tags = v.findViewById<EditText>(R.id.add_profile_tags)
        tags.hint = resources.getString(R.string.add_tags, Utils.SettingData.DefaultSplitChar)
        // 设置点击事件
        val imageUpload = v.findViewById<ImageButton>(R.id.add_profile_avators_add)
        imageUpload.setOnClickListener(onImageUploadClick(context, tab))
        val birthdayselect = v.findViewById<TextView>(R.id.add_profile_birthday_select)
        birthdayselect.setOnClickListener(OnBirthdaySelectClick())
        val birthdayclear = v.findViewById<TextView>(R.id.add_profile_birthday_clear)
        birthdayclear.setOnClickListener(OnBirthdayClearClick())
        val cancel = v.findViewById<TextView>(R.id.add_profile_cancel)
        cancel.setOnClickListener(OnCancelClick(dialog!!))
        val confirm = v.findViewById<TextView>(R.id.add_profile_confirm)
        confirm.setOnClickListener(OnConfirmClick(dialog, index))
    }

    // 添加头像
    fun addAvator(bitmap: Bitmap, path: String) {
        if (avatorList.contains(path)) {
            PopTip.show("此图片已上传")
                .setBackgroundColor(resources.getColor(R.color.warning))
                .setMessageTextInfo(TextInfo().apply {
                    this.fontColor = resources.getColor(R.color.white)
                })
            return
        }
        val layout = view.findViewById<GridLayout>(R.id.add_profile_avators)
        avatorList += path
        val image = ImageView(context)
        image.layoutParams = ViewGroup.LayoutParams(
            Utils.dip2px(context, 60f),
            Utils.dip2px(context, 60f)
        )
        image.setImageBitmap(bitmap)
        image.setOnClickListener(onImageClick(path))
        layout.addView(image)
    }

    // 移除头像
    fun removeAvator(item: String) {
        val layout = view.findViewById<GridLayout>(R.id.add_profile_avators)
        val index = avatorList.indexOf(item)
        val removeItem = avatorList[index]
        avatorList -= removeItem
        val image = layout.getChildAt(index + 1)
        layout.removeView(image)
    }

    // 加载学生资料
    fun loadProfile(profile: Profile) {
        // 加载学生信息到对话框中
        val name = view.findViewById<EditText>(R.id.add_profile_name)
        val firstname = view.findViewById<EditText>(R.id.add_profile_firstname)
        val age = view.findViewById<EditText>(R.id.add_profile_age)
        val height = view.findViewById<EditText>(R.id.add_profile_height)
        val birthday = view.findViewById<TextView>(R.id.add_profile_birthday)
        val school = view.findViewById<EditText>(R.id.add_profile_school)
        val hobbies = view.findViewById<EditText>(R.id.add_profile_hobbies)
        val momotalkstate = view.findViewById<EditText>(R.id.add_profile_momotalkstate)
        val description = view.findViewById<EditText>(R.id.add_profile_description)
        val tags = view.findViewById<EditText>(R.id.add_profile_tags)

        name.setText(profile.Name)
        profile.FirstName?.let { firstname.setText(it) }
        profile.Age?.let { age.setText(it.toString()) }
        profile.Height?.let { height.setText(it.toString()) }
        profile.BirthDay?.let {
            this.birthday = it
            birthday.text = resources.getString(R.string.profile_birthday, it.Month.toString(), it.Day.toString())
        }
        profile.School?.let { school.setText(it) }
        profile.Hobbies?.let { hobbies.setText(it.joinToString(Utils.SettingData.DefaultSplitChar)) }
        profile.MomotalkState?.let { momotalkstate.setText(it) }
        profile.Description?.let { description.setText(it) }
        profile.Tags?.let { tags.setText(it.joinToString(Utils.SettingData.DefaultSplitChar)) }
    }

    inner class onImageUploadClick(val context: Context, val tab: ProfileTab) : View.OnClickListener {
        override fun onClick(v: View?) {
            // 请求读取图片权限
            XXPermissions.with(context)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(object : OnPermissionCallback {

                    override fun onGranted(
                        permissions: MutableList<String>,
                        allGranted: Boolean
                    ) {
                        uploadImage()
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Toast.makeText(context, "请手动授予权限并重新上传图片", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "请授予权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }

        fun uploadImage() {
            // 打开相册选择图片
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            tab.startActivityForResult(intent, 1)
        }
    }

    // 点击图片事件
    inner class onImageClick(val

                             item: String) : View.OnClickListener {
        override fun onClick(v: View?) {
            PopMenu.show(v, listOf("删除"))
                .setOverlayBaseView(false)
                .setAlignGravity(Gravity.BOTTOM)
                .setWidth(Utils.dip2px(context, 80f))
                .setOnMenuItemClickListener { dialog, _, _ ->
                    dialog.dismiss()
                    removeAvator(item)
                    false
                }
        }
    }

    // 选择生日事件
    inner class OnBirthdaySelectClick() : View.OnClickListener {
        override fun onClick(v: View?) {
            val birthdayview = view.findViewById<TextView>(R.id.add_profile_birthday)
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                R.style.dialog_date,
                { _, _, monthOfYear, dayOfMonth ->
                    run {
                        birthday = Birthday(monthOfYear + 1, dayOfMonth)
                        birthdayview.text = resources.getString(
                            R.string.profile_birthday,
                            (monthOfYear + 1).toString(),
                            dayOfMonth.toString()
                        )
                    }
                },
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH],
            )
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(
                resources.getColor(R.color.momotalk_pink1)
            )
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(
                resources.getColor(R.color.momotalk_pink1)
            )
        }
    }

    // 清除生日事件
    inner class OnBirthdayClearClick() : View.OnClickListener {
        override fun onClick(v: View?) {
            val birthday = view.findViewById<TextView>(R.id.add_profile_birthday)
            birthday.text = resources.getString(R.string.add_profile_birthday_unselected)
        }
    }

    // 取消事件
    inner class OnCancelClick(val dialog: CustomDialog) : View.OnClickListener {
        override fun onClick(v: View?) {
            dialog.dismiss()
        }
    }

    // 确认事件
    inner class OnConfirmClick(val dialog: CustomDialog, val index: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val name = view.findViewById<EditText>(R.id.add_profile_name)
            val layout = view.findViewById<GridLayout>(R.id.add_profile_avators)
            if (name.text.toString() == "" || layout.childCount == 1) {
                if (name.text.toString() == "") {
                    PopTip.show("请输入姓名")
                        .setBackgroundColor(resources.getColor(R.color.warning))
                        .setMessageTextInfo(TextInfo().apply {
                            this.fontColor = resources.getColor(R.color.white)
                        })
                }
                if (layout.childCount == 1) {
                    PopTip.show("请上传至少一张头像")
                        .setBackgroundColor(resources.getColor(R.color.warning))
                        .setMessageTextInfo(TextInfo().apply {
                            this.fontColor = resources.getColor(R.color.white)
                        })
                }
                return
            }
            var imageList: List<Image> = listOf()
            val firstname = view.findViewById<EditText>(R.id.add_profile_firstname)
            for (index: Int in 0 until avatorList.size) {
                val file = File(avatorList[index])
                val newfile = File(Utils.appDataPath + "/" + Utils.toMD5(file.name))
                if (newfile.exists()) {
                    Files.copy(file.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.copy(file.toPath(), newfile.toPath())
                }
                val image = Image(name.text.toString() + "[" + index + "]", newfile.absolutePath, true)
                imageList += image
            }
            val age = view.findViewById<EditText>(R.id.add_profile_age)
            val height = view.findViewById<EditText>(R.id.add_profile_height)
            val birthdayView = view.findViewById<TextView>(R.id.add_profile_birthday)
            val school = view.findViewById<EditText>(R.id.add_profile_school)
            val hobbies = view.findViewById<EditText>(R.id.add_profile_hobbies)
            val momotalkstate = view.findViewById<EditText>(R.id.add_profile_momotalkstate)
            val description = view.findViewById<EditText>(R.id.add_profile_description)
            val tags = view.findViewById<EditText>(R.id.add_profile_tags)

            val profile = Profile(
                name.text.toString(),
                if (firstname.text.toString() == "") null else firstname.text.toString(),
                imageList,
                if (age.text.toString() == "") null else age.text.toString().toInt(),
                if (height.text.toString() == "") null else height.text.toString().toInt(),
                if (birthdayView.text == resources.getString(R.string.add_profile_birthday_unselected)) null else birthday,
                if (school.text.toString() == "") null else school.text.toString(),
                if (hobbies.text.toString() == "") null else hobbies.text.toString()
                    .split(Utils.SettingData.DefaultSplitChar),
                if (momotalkstate.text.toString() == "") null else momotalkstate.text.toString(),
                if (description.text.toString() == "") null else description.text.toString(),
                if (tags.text.toString() == "") null else tags.text.toString()
                    .split(Utils.SettingData.DefaultSplitChar)
            )
            if (isRewrite) {
                tab.replaceProfile(index, profile)
            } else {
                tab.addProfile(profile)
            }
            dialog.dismiss()
            Utils.save()
        }
    }
}
