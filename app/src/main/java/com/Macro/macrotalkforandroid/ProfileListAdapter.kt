package com.Macro.macrotalkforandroid

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ProfileListAdapter(private val context: Context, var profileList: List<Profile>, val isPrefab : Boolean) :
    RecyclerView.Adapter<ProfileListAdapter.ProfileViewHodler>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHodler {
        val itemView = LayoutInflater.from(context).inflate(R.layout.profile_item, parent, false)
        return ProfileViewHodler(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHodler, position: Int) {
        val data: Profile = profileList[position]
        if (!data.Images[0].isNotPrefab) {
            val input = context.assets.open(data.Images[0].ImageName + ".jpg")
            holder.profileListImage.setImageBitmap(BitmapFactory.decodeStream(input))
        } else {
            val bitmap = BitmapFactory.decodeFile(data.Images[0].ImageOriginalUri)
            holder.profileListImage.setImageBitmap(bitmap)
        }
        holder.profileListName.text = data.Name
        holder.profileListMomotalkState.text = data.MomotalkState
        if (data.School != null) {
            try {
                val school = data.School?.let { context.assets.open("$it.png") }
                holder.profileListSchoolImage.setImageBitmap(BitmapFactory.decodeStream(school))
            }
            catch (_: Exception) {}
            try {
                holder.profileListSchoolImage.setImageBitmap(BitmapFactory.decodeFile(data.School))
            }
            catch (_: Exception) {}
        }
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    override fun onViewAttachedToWindow(holder: ProfileViewHodler) {
        super.onViewAttachedToWindow(holder)

        val animation = AnimationUtils.loadAnimation(holder.view.context, R.anim.list_item_anim)
        holder.view.startAnimation(animation)
    }

    fun addItem(profile : Profile) {
        profileList += profile
        notifyItemInserted(profileList.size - 1)
    }

    fun removeItem(profile : Profile, index : Int) {
        profileList -= profile
        notifyItemRemoved(index)
    }

    fun replaceItem(profile : Profile, index : Int) {
        val list = profileList.toMutableList()
        list[index] = profile
        profileList = list.toList()
        notifyItemChanged(index)
    }

    inner class ProfileViewHodler(itemView : View) : ViewHolder(itemView) {
        val profileListImage : ImageView
        val profileListName : TextView
        val profileListMomotalkState : TextView
        val profileListSchoolImage : ImageView

        val view : View

        init {
            profileListImage = itemView.findViewById<View>(R.id.profilelist_Image) as ImageView
            profileListName = itemView.findViewById<View>(R.id.profilelist_Name) as TextView
            profileListMomotalkState = itemView.findViewById<View>(R.id.profilelist_MomotalkState) as TextView
            profileListSchoolImage = itemView.findViewById<View>(R.id.profilelist_SchoolImage) as ImageView

            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, profileList[layoutPosition])
                }
            }

            if (!isPrefab) {
                itemView.setOnLongClickListener { v ->
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener!!.OnItemLongClick(v, profileList[layoutPosition])
                    }
                    true
                }
            }

            view = itemView
        }
    }

    interface OnItemClickListener {
        fun OnItemClick(view: View?, data: Profile?)
    }

    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, data : Profile?)
    }

    private var onItemClickListener : OnItemClickListener? = null

    private var onItemLongClickListener : OnItemLongClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener
    }
}