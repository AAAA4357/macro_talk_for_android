package com.macro.macrotalkforandroid.ui.profilelist.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.R

class ProfileAvatorAdapter()
    : RecyclerView.Adapter<ProfileAvatorAdapter.ProfileAvatorViewHolder>() {

    var avatorList : List<Image> = listOf()
    lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAvatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_avator,parent,false)
        return ProfileAvatorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return avatorList.size
    }

    override fun onBindViewHolder(holder: ProfileAvatorViewHolder, position: Int) {
        holder.bindImage(avatorList[position])
    }

    fun setData(avators : List<Image>){
        avatorList = avators
        notifyDataSetChanged()
    }

    inner class ProfileAvatorViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val profileImage : ImageView

        init {
            profileImage = itemView.findViewById(R.id.profile_avator)
        }

        fun bindImage(avator : Image) {
            if (!avator.isNotPrefab) {
                val input = context.assets.open(avator.ImageName + ".jpg")
                profileImage.setImageBitmap(BitmapFactory.decodeStream(input))
            } else {
                profileImage.setImageBitmap(BitmapFactory.decodeFile(avator.ImageOriginalUri))
            }
        }
    }
}