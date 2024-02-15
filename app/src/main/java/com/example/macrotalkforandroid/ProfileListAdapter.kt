package com.example.macrotalkforandroid

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

internal class ProfileListAdapter(private val context: Context, profileList: Array<Profile>) :
    RecyclerView.Adapter<ProfileListAdapter.ProfileViewHodler>() {

    private val profileList: Array<Profile>

    init {
        this.profileList = profileList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHodler {
        val itemView = View.inflate(context, R.layout.profile_item, null)
        return ProfileViewHodler(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHodler, position: Int) {
        val data: Profile = profileList[position]
        val input = context.assets.open(data.Images[0].ImageName + ".jpg")
        holder.profileListImage.setImageBitmap(BitmapFactory.decodeStream(input))
        holder.profileListName.text = data.Name
        holder.profileListMomotalkState.text = data.MomotalkState
        try {
            val school = data.School?.let { context.assets.open("$it.png") }
            holder.profileListSchoolImage.setImageBitmap(BitmapFactory.decodeStream(school))
        }
        catch (_: Exception) {}
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    override fun onViewAttachedToWindow(holder: ProfileViewHodler) {
        super.onViewAttachedToWindow(holder)

        val animation = AnimationUtils.loadAnimation(holder.view.context, R.anim.list_item_anim)
        holder.view.startAnimation(animation)
    }

    inner class ProfileViewHodler(itemView: View) : ViewHolder(itemView) {
        val profileListImage: ImageView
        val profileListName: TextView
        val profileListMomotalkState: TextView
        val profileListSchoolImage: ImageView

        val view : View

        init {
            profileListImage = itemView.findViewById<View>(R.id.conversationlist_Image) as ImageView
            profileListName = itemView.findViewById<View>(R.id.profilelist_Name) as TextView
            profileListMomotalkState = itemView.findViewById<View>(R.id.profilelist_MomotalkState) as TextView
            profileListSchoolImage = itemView.findViewById<View>(R.id.profilelist_SchoolImage) as ImageView

            itemView.setOnClickListener { v ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(v, profileList[layoutPosition])
                }
            }

            view = itemView
        }
    }

    interface OnItemClickListener {
        fun OnItemClick(view: View?, data: Profile?)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }
}