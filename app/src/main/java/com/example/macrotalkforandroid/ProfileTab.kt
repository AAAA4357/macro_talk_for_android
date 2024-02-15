package com.example.macrotalkforandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextPaint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView


class ProfileTab() : Fragment() {
    lateinit var profileList : List<Profile>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isPrefab = requireArguments().getBoolean("isPrefab")
        profileList = if (isPrefab) Utils.prefabData.Profiles else Utils.storageData.Profiles

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
            list.visibility = View.INVISIBLE
        }
        else {
            empty.visibility = View.INVISIBLE
            list.visibility = View.VISIBLE
        }
        val isPrefab = requireArguments().getBoolean("isPrefab")
        if (isPrefab) {
            val addProfile = requireView().findViewById<FloatingActionButton>(R.id.profile_addprofile)
            addProfile.visibility = View.INVISIBLE
        } else {
            val addProfile = requireView().findViewById<FloatingActionButton>(R.id.profile_addprofile)
            addProfile.setOnClickListener(AddProfileClick())
        }
        val adapter = ProfileListAdapter(requireActivity(), profileList.toTypedArray())
        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        val itemDecoration = SuspendItemDecoration(requireContext()).apply {
            dividerDrawable = resources.getDrawable(R.drawable.list_divider)
            groupDividerDrawable = resources.getDrawable(R.drawable.list_divider_45dp)
            profiles = profileList
        }
        list.apply {
            this.adapter = adapter
            this.layoutManager = layoutManager
            addItemDecoration(itemDecoration)
        }
    }

    fun AddProfile(profile : Profile) {
        profileList += profile
        val empty = requireView().findViewById<TextView>(R.id.profile_empty)
        empty.visibility = View.INVISIBLE
    }

    fun RemoveProfile(profile : Profile) {
        profileList -= profile
        if (profileList.isEmpty()) {
            val empty = requireView().findViewById<TextView>(R.id.profile_empty)
            empty.visibility = View.VISIBLE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(isPrefab : Boolean) : ProfileTab {
            val args = Bundle()
            args.putBoolean("isPrefab", isPrefab)
            val fragment = ProfileTab()
            fragment.arguments = args
            return fragment
        }
    }

    inner class AddProfileClick : OnClickListener {
        override fun onClick(v: View?) {
            val dialog = CustomDialog.build()
            dialog.setCustomView(AddProfileBindView())
            dialog.setMaskColor(resources.getColor(R.color.trans_lightgray))
            dialog.show()
        }

        inner class AddProfileBindView() : OnBindView<CustomDialog>(R.layout.fragement_addprofile) {
            override fun onBind(dialog: CustomDialog?, v: View?) {

            }

        }
    }

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
}