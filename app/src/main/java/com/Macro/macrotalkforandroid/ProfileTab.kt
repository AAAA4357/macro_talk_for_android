package com.Macro.macrotalkforandroid

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import com.Macro.macrotalkforandroid.R
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Calendar

class ProfileTab(val isPrefab : Boolean) : Fragment() {
    lateinit var profileList : List<Profile>

    lateinit var profileAdapter : ProfileListAdapter

    lateinit var addProfileClick : AddProfileClick

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileList = if (isPrefab) Utils.prefabData.Profiles else Utils.storageData.Profiles

        profileAdapter = ProfileListAdapter(requireActivity(), profileList, isPrefab)

        if (!isPrefab) {
            addProfileClick = AddProfileClick(resources, requireContext(), this@ProfileTab)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_tab, container, false)
    }

    override fun onStart() {
        super.onStart()

        val list = requireView().findViewById<RecyclerView>(R.id.profile_list)
        val empty = requireView().findViewById<TextView>(R.id.profile_empty)
        if (profileList.isEmpty()) {
            empty.visibility = View.VISIBLE
        }
        else {
            empty.visibility = View.INVISIBLE
        }
        val isPrefab = requireArguments().getBoolean("isPrefab")
        if (isPrefab) {
            val addProfile = requireView().findViewById<FloatingActionButton>(R.id.profile_addprofile)
            addProfile.visibility = View.INVISIBLE
        } else {
            val addProfile = requireView().findViewById<FloatingActionButton>(R.id.profile_addprofile)
            addProfile.setOnClickListener(addProfileClick)
        }
        profileAdapter.setOnItemClickListener(OnProfileItemClick())
        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        val itemDecoration = SuspendItemDecoration(requireContext()).apply {
            dividerDrawable = resources.getDrawable(R.drawable.ic_list_divider)
            groupDividerDrawable = resources.getDrawable(R.drawable.ic_list_divider_wider)
            profiles = profileList
        }
        if (list.itemDecorationCount == 0 && isPrefab) {
            list.apply {
                this.adapter = profileAdapter
                this.layoutManager = layoutManager
                addItemDecoration(itemDecoration)
            }
        } else if (list.itemDecorationCount == 0 && !isPrefab) {
            profileAdapter.setOnItemLongClickListener(OnProfileItemLongClick())
            list.apply {
                this.adapter = profileAdapter
                this.layoutManager = layoutManager
            }
        }
    }

    inner class OnProfileItemClick() : ProfileListAdapter.OnItemClickListener {
        override fun OnItemClick(view: View?, data: Profile?) {
            ProfileActivity.displayProfile = data!!
            ProfileActivity.isPrefab = isPrefab
            val intent = Intent(requireContext(), ProfileActivity()::class.java)
            startActivity(intent)
        }
    }

    inner class OnProfileItemLongClick() : ProfileListAdapter.OnItemLongClickListener {
        var modifyProfileView = addProfileClick.AddProfileBindView(true)

        override fun OnItemLongClick(v: View?, data: Profile?) {
            PopMenu.show(v, listOf("删除", "修改"))
                .setOverlayBaseView(false)
                .setAlignGravity(Gravity.BOTTOM)
                .setWidth(Utils.dip2px(requireContext(), 80f))
                .setOnMenuItemClickListener { dialog, _, index ->
                    dialog.dismiss()
                    when (index) {
                        0 -> {
                            removeProfile(data!!)
                        }
                        1 -> {
                            modifyProfileView.index = profileList.indexOf(data!!)
                            val dialog = CustomDialog.build()
                            dialog.setCustomView(modifyProfileView)
                            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
                            dialog.show()
                            for (i in 0..<modifyProfileView.avatorList.size) {
                                modifyProfileView.removeAvator(modifyProfileView.avatorList[0])
                            }
                            for (image in data.Images) {
                                modifyProfileView.addAvator(BitmapFactory.decodeFile(image.ImageOriginalUri), image.ImageOriginalUri)
                            }
                            modifyProfileView.refreshProfile(data)
                        }
                        else -> {}
                    }
                    false
                }
        }

    }

    fun addProfile(profile : Profile) {
        profileList += profile
        profileAdapter.addItem(profile)
        val empty = requireView().findViewById<TextView>(R.id.profile_empty)
        empty.visibility = View.INVISIBLE
    }

    fun removeProfile(profile : Profile) {
        val index = profileList.indexOf(profile)
        profileList -= profile
        profileAdapter.removeItem(profile, index)
        if (profileList.isEmpty()) {
            val empty = requireView().findViewById<TextView>(R.id.profile_empty)
            empty.visibility = View.VISIBLE
        }
    }

    fun replaceProfile(index : Int, profile : Profile) {
        val list = profileList.toMutableList()
        list[index] = profile
        profileList = list.toList()
        profileAdapter.replaceItem(profile, index)
    }

    companion object {
        fun newInstance(isPrefab : Boolean) : ProfileTab {
            val args = Bundle()
            args.putBoolean("isPrefab", isPrefab)
            val fragment = ProfileTab(isPrefab)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val path = data.data!!.path!!.replace("/raw/", "")
            val bitmap = BitmapFactory.decodeFile(path)
            addProfileClick.addProfileView.addAvator(bitmap, path)
        }
    }
}

class AddProfileClick(val resources : Resources, val context : Context, val tab : ProfileTab) : OnClickListener {
    var addProfileView = AddProfileBindView(false)

    override fun onClick(v: View?) {
        addProfileView = AddProfileBindView(false)
        val dialog = CustomDialog.build()
        dialog.setCustomView(addProfileView)
        dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
        dialog.show()
    }

    inner class AddProfileBindView(val isRewrite : Boolean) : OnBindView<CustomDialog>(R.layout.fragement_addprofile) {
        var avatorList : List<String> = listOf()
        var birthday = Birthday(1, 1)

        var index = -1

        lateinit var view : View

        override fun onBind(dialog: CustomDialog?, v: View?) {
            view = v!!
            val hobbies = v.findViewById<EditText>(R.id.add_profile_hobbies)
            hobbies.hint = resources.getString(R.string.add_profile_hobbies, Utils.SettingData.DefaultSplitChar)
            val tags = v.findViewById<EditText>(R.id.add_profile_tags)
            tags.hint = resources.getString(R.string.add_profile_tags, Utils.SettingData.DefaultSplitChar)
            val imageUpload = v.findViewById<ImageButton>(R.id.add_profile_avators_add)
            imageUpload.setOnClickListener(onImageUploadClick(context, tab))
            val birthdayselect = v.findViewById<TextView>(R.id.add_profile_birthday_select)
            birthdayselect.setOnClickListener(OnBirthdaySelectClick(this@AddProfileBindView, context, resources))
            val birthdayclear = v.findViewById<TextView>(R.id.add_profile_birthday_clear)
            birthdayclear.setOnClickListener(OnBirthdayClearClick(view, resources))
            val cancel = v.findViewById<TextView>(R.id.add_profile_cancel)
            cancel.setOnClickListener(OnCancelClick(dialog!!))
            val confirm = v.findViewById<TextView>(R.id.add_profile_confirm)
            confirm.setOnClickListener(OnConfirmClick(dialog, this@AddProfileBindView, v, resources, tab, isRewrite, index))
        }

        fun addAvator(bitmap : Bitmap, path : String) {
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
            image.layoutParams = ViewGroup.LayoutParams(Utils.dip2px(context, 60f), Utils.dip2px(context, 60f))
            image.setImageBitmap(bitmap)
            image.setOnClickListener(onImageClick(this@AddProfileBindView, context, path))
            layout.addView(image)
        }

        fun removeAvator(item : String) {
            val layout = view.findViewById<GridLayout>(R.id.add_profile_avators)
            val index = avatorList.indexOf(item)
            val removeItem = avatorList[index]
            avatorList -= removeItem
            val image = layout.getChildAt(index + 1)
            layout.removeView(image)
        }

        fun refreshProfile(profile : Profile) {
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
    }
}

class OnBirthdaySelectClick(val ProfileView: AddProfileClick.AddProfileBindView, val context : Context, val resources: Resources) : OnClickListener {
    override fun onClick(v: View?) {
        val birthday = ProfileView.view.findViewById<TextView>(R.id.add_profile_birthday)
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, _, monthOfYear, dayOfMonth ->
                run {
                    ProfileView.birthday = Birthday(monthOfYear, dayOfMonth)
                    birthday.text = resources.getString(R.string.profile_birthday, monthOfYear.toString(), dayOfMonth.toString())
                }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        datePickerDialog.show()
    }
}

class OnBirthdayClearClick(val view : View, val resources : Resources) : OnClickListener {
    override fun onClick(v: View?) {
        val birthday = view.findViewById<TextView>(R.id.add_profile_birthday)
        birthday.text = resources.getString(R.string.add_profile_birthday_unselected)
    }
}

class OnCancelClick(val dialog : CustomDialog) : OnClickListener {
    override fun onClick(v: View?) {
        dialog.dismiss()
    }
}

class OnConfirmClick(val dialog : CustomDialog,
                     val profileBindView : AddProfileClick.AddProfileBindView,
                     val view : View,
                     val resources : Resources,
                     val profileTab : ProfileTab,
                     val isRewrite : Boolean,
                     val index : Int) : OnClickListener {
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
        val firstname = view.findViewById<EditText>(R.id.add_profile_firstname)
        var avatorList : List<Image> = listOf()
        for (index : Int in 0..profileBindView.avatorList.size - 1) {
            val file = File(profileBindView.avatorList[index])
            val newfile = File(Utils.appDataPath + "/" + Utils.toMD5(file.name))
            if (newfile.exists()) {
                Files.copy(file.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } else {
                Files.copy(file.toPath(), newfile.toPath())
            }
            val image = Image(name.text.toString() + "[" + index + "]", newfile.absolutePath)
            avatorList += image
        }

        val age = view.findViewById<EditText>(R.id.add_profile_age)
        val height = view.findViewById<EditText>(R.id.add_profile_height)
        val birthday = view.findViewById<TextView>(R.id.add_profile_birthday)
        val school = view.findViewById<EditText>(R.id.add_profile_school)
        val hobbies = view.findViewById<EditText>(R.id.add_profile_hobbies)
        val momotalkstate = view.findViewById<EditText>(R.id.add_profile_momotalkstate)
        val description = view.findViewById<EditText>(R.id.add_profile_description)
        val tags = view.findViewById<EditText>(R.id.add_profile_tags)

        val profile = Profile(
            name.text.toString(),
            if (firstname.text.toString() == "") null else firstname.text.toString(),
            avatorList,
            if (age.text.toString() == "") null else age.text.toString().toInt(),
            if (height.text.toString() == "") null else height.text.toString().toInt(),
            if (birthday.text == resources.getString(R.string.add_profile_birthday_unselected)) null else this.profileBindView.birthday,
            if (school.text.toString() == "") null else school.text.toString(),
            if (hobbies.text.toString() == "") null else hobbies.text.toString().split(Utils.SettingData.DefaultSplitChar),
            if (momotalkstate.text.toString() == "") null else momotalkstate.text.toString(),
            if (description.text.toString() == "") null else description.text.toString(),
            if (tags.text.toString() == "") null else tags.text.toString().split(Utils.SettingData.DefaultSplitChar),
        )
        if (isRewrite) {
            profileTab.replaceProfile(index, profile)
        } else {
            profileTab.addProfile(profile)
        }
        dialog.dismiss()
    }
}

class onImageClick(val addProfileView : AddProfileClick.AddProfileBindView, val context : Context, val item : String) : OnClickListener {
    override fun onClick(v: View?) {
        PopMenu.show(v, listOf("删除"))
            .setOverlayBaseView(false)
            .setAlignGravity(Gravity.BOTTOM)
            .setWidth(Utils.dip2px(context, 80f))
            .setOnMenuItemClickListener { dialog, _, _ ->
                dialog.dismiss()
                addProfileView.removeAvator(item)
                false
            }
    }
}

class onImageUploadClick(val context : Context, val tab : ProfileTab) : OnClickListener {
    override fun onClick(v: View?) {
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
        val intent = Intent(Intent.ACTION_PICK,  null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        tab.startActivityForResult(intent, 1)
    }
}

class SuspendItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {

    var dividerDrawable: Drawable? = null
    var groupDividerDrawable: Drawable? = null
    var profiles: List<Profile>? = null

    private val bounds = Rect()
    private val textPaint: TextPaint
    @ColorRes
    var textColor: Int = android.R.color.white
        set(value) {
            field = value
            textPaint.color = context.resources.getColor(value)
        }
    var textSize: Float = 20F
        set(value) {
            field = value
            textPaint.textSize = context.sp2px(value)
        }

    var textTypeface = Typeface.DEFAULT_BOLD!!
        set(value) {
            field = value
            textPaint.typeface = value
        }

    init {
        val tapeArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        dividerDrawable = tapeArray.getDrawable(0)
        tapeArray.recycle()

        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        textPaint.color = context.resources.getColor(textColor)
        textPaint.textSize = context.sp2px(textSize)
        textPaint.typeface = textTypeface
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val list = profiles ?: return

        c.save()

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.viewLayoutPosition

            val currentInitial = PinyinHelper.toPinyin(list[position].Name[0].toString(), PinyinStyleEnum.FIRST_LETTER).toString().uppercase()
            val lastInitial = if (position >= 1) {
                PinyinHelper.toPinyin(list[position - 1].Name[0].toString(), PinyinStyleEnum.FIRST_LETTER).toString().uppercase()
            } else {
                null
            }

            var isDrawText = false
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val drawable = if (currentInitial == lastInitial) {
                dividerDrawable
            } else {
                isDrawText = true
                groupDividerDrawable
            }

            val top = bounds.top
            val bottom = top + (drawable?.intrinsicHeight ?: 0)
            drawable?.setBounds(left, top, right, bottom)
            drawable?.draw(c)
            if (isDrawText) {
                if (drawable != null) {
                    textPaint.getTextBounds(currentInitial, 0, currentInitial.length, bounds)
                    val textX = child.paddingLeft.toFloat() + 20
                    val textY = (child.top - (drawable.intrinsicHeight - bounds.height()) / 2).toFloat()
                    c.drawText(currentInitial, textX, textY, textPaint)
                }
            }
        }
        c.restore()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val list = profiles ?: return
        val drawable = groupDividerDrawable ?: return

        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return

        val position = layoutManager.findFirstVisibleItemPosition()
        if (position < 0 || position > list.size - 1) {
            return
        }

        val child = parent.findViewHolderForLayoutPosition(position)!!.itemView

        val currentInitial = PinyinHelper.toPinyin(list[position].Name[0].toString(), PinyinStyleEnum.FIRST_LETTER).toString().uppercase()
        val nextInitial = if (position + 1 < list.size) {
            PinyinHelper.toPinyin(list[position + 1].Name[0].toString(), PinyinStyleEnum.FIRST_LETTER).toString().uppercase()
        } else {
            null
        }

        parent.getDecoratedBoundsWithMargins(child, bounds)

        c.save()
        if (currentInitial != nextInitial) {
            if (child.top + child.height < drawable.intrinsicHeight) {
                c.translate(0f, (child.height + child.top - drawable.intrinsicHeight).toFloat())
            }
        }

        val left = parent.paddingLeft
        val top = parent.paddingTop
        val right = parent.right - parent.paddingRight
        val bottom = parent.paddingTop + drawable.intrinsicHeight

        drawable.setBounds(left, top, right, bottom)
        drawable.draw(c)

        textPaint.getTextBounds(currentInitial, 0, currentInitial.length, bounds)
        val textX = child.paddingLeft.toFloat() + 20
        val textY =
            (parent.paddingTop + drawable.intrinsicHeight - (drawable.intrinsicHeight - bounds.height()) / 2).toFloat()
        c.drawText(currentInitial, textX, textY, textPaint)
        c.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, 0)
        val list = profiles ?: return

        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition

        val currentInitial = PinyinHelper.toPinyin(list[position].Name[0].toString(), PinyinStyleEnum.FIRST_LETTER).toString().uppercase()
        val lastInitial = if (position >= 1) {
            PinyinHelper.toPinyin(list[position - 1].Name[0].toString(), PinyinStyleEnum.FIRST_LETTER).toString().uppercase()
        } else {
            null
        }

        val drawable = if (currentInitial == lastInitial) {
            dividerDrawable
        } else {
            groupDividerDrawable
        }

        val height = drawable?.intrinsicHeight ?: 0
        outRect.set(0, height, 0, 0)
    }

    fun Context.sp2px(sp : Float) : Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
}