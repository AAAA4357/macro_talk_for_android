package com.macro.macrotalkforandroid.ui.profilelist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.houbb.heaven.util.io.FileUtil
import com.macro.macrotalkforandroid.Profile
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import com.macro.macrotalkforandroid.ui.profilelist.profile.ProfileActivity
import java.io.File

// 学生资料列表的标签页
class ProfileTab(val isPrefab : Boolean) : Fragment() {
    lateinit var profileList : List<Profile>

    lateinit var profileAdapter : ProfileListAdapter

    lateinit var addProfileClick : AddProfileClick

    companion object {
        // 创建新的 ProfileTab 实例
        fun newInstance(isPrefab : Boolean) : ProfileTab {
            val args = Bundle()
            args.putBoolean("isPrefab", isPrefab)
            val fragment = ProfileTab(isPrefab)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 根据是否为预制档案初始化学生资料列表
        profileList = if (isPrefab) Utils.prefabData.Profiles else Utils.storageData.Profiles

        // 初始化学生资料列表适配器
        profileAdapter = ProfileListAdapter(requireActivity(), profileList, isPrefab)

        // 如果不是预制档案，初始化添加资料按钮点击事件处理
        if (!isPrefab) {
            addProfileClick = AddProfileClick()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局文件
        return inflater.inflate(R.layout.fragment_profile_tab, container, false)
    }

    override fun onStart() {
        super.onStart()

        val list = requireView().findViewById<RecyclerView>(R.id.profile_list)
        val empty = requireView().findViewById<TextView>(R.id.profile_empty)

        // 如果学生资料列表为空，显示空提示
        if (profileList.isEmpty()) {
            empty.visibility = View.VISIBLE
        } else {
            empty.visibility = View.INVISIBLE
        }

        // 根据是否为预制档案，设置添加资料按钮的可见性和点击事件
        val isPrefab = requireArguments().getBoolean("isPrefab")
        if (isPrefab) {
            val addProfile = requireView().findViewById<FloatingActionButton>(R.id.profile_addprofile)
            addProfile.visibility = View.INVISIBLE
        } else {
            val addProfile = requireView().findViewById<FloatingActionButton>(R.id.profile_addprofile)
            addProfile.setOnClickListener(addProfileClick)
        }

        // 设置学生资料列表适配器和布局管理器
        list.apply {
            this.adapter = profileAdapter
            this.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 处理从相册选择头像后的结果
        if (resultCode == Activity.RESULT_OK && data != null) {
            val file = Utils.uriToFile(requireContext(), data.data!!)!!
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            addProfileClick.addProfileView.addAvator(bitmap, file.absolutePath)
        }
    }

    // 添加学生资料
    fun addProfile(profile : Profile) {
        profileList += profile
        profileAdapter.addItem(profile)
        val empty = requireView().findViewById<TextView>(R.id.profile_empty)
        empty.visibility = View.INVISIBLE
    }

    // 删除学生资料
    fun removeProfile(profile : Profile) {
        val index = profileList.indexOf(profile)
        profileList -= profile
        profileAdapter.removeItem(profile, index)
        if (profileList.isEmpty()) {
            val empty = requireView().findViewById<TextView>(R.id.profile_empty)
            empty.visibility = View.VISIBLE
        }
    }

    // 替换学生资料
    fun replaceProfile(index : Int, profile : Profile) {
        val list = profileList.toMutableList()
        list[index] = profile
        profileList = list.toList()
        profileAdapter.replaceItem(profile, index)
    }

    // 学生点击学生资料列表项的处理
    inner class OnProfileItemClick() : ProfileListAdapter.OnItemClickListener {
        override fun OnItemClick(view: View?, data: Profile?) {
            ProfileActivity.displayProfile = data!!
            val intent = Intent(requireContext(), ProfileActivity()::class.java)
            startActivity(intent)
        }
    }

    // 学生长按学生资料列表项的处理
    inner class OnProfileItemLongClick() : ProfileListAdapter.OnItemLongClickListener {
        var modifyProfileView = AddProfileBindView(true, resources, requireContext(), this@ProfileTab)

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
                            for (i in 0 until modifyProfileView.avatorList.size) {
                                modifyProfileView.removeAvator(modifyProfileView.avatorList[0])
                            }
                            for (image in data.Images) {
                                modifyProfileView.addAvator(BitmapFactory.decodeFile(image.ImageOriginalUri), image.ImageOriginalUri)
                            }
                            modifyProfileView.loadProfile(data)
                        }
                        else -> {}
                    }
                    false
                }
        }
    }

    // 添加学生资料按钮的点击事件处理
    inner class AddProfileClick() : OnClickListener {
        var addProfileView = AddProfileBindView(false, resources, requireContext(), this@ProfileTab)

        override fun onClick(v: View?) {
            addProfileView = AddProfileBindView(false, resources, requireContext(), this@ProfileTab)
            val dialog = CustomDialog.build()
            dialog.setCustomView(addProfileView)
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.show()
        }
    }

    // 设置分组悬浮标题的 RecyclerView.ItemDecoration
    inner class SuspendItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {

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

        // 绘制分组标题和分割线
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

                // 获取当前学生资料的首字母拼音
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

        // 绘制悬浮标题
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

            // 获取当前可见分组的首字母拼音
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

        // 设置分组标题和分割线的偏移量
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(0, 0, 0, 0)
            val list = profiles ?: return

            val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition

            // 获取当前学生资料的首字母拼音
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

        // 将 sp 单位转换为 px
        fun Context.sp2px(sp : Float) : Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }
}

