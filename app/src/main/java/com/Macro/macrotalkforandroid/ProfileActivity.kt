package com.Macro.macrotalkforandroid

import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.Macro.macrotalkforandroid.R

class ProfileActivity() : AppCompatActivity() {
    companion object {
        lateinit var displayProfile : Profile

        var isPrefab : Boolean? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setTitle(R.string.profile_title)

        val avators = findViewById<ViewPager2>(R.id.profile_avators)
        val adapter = ProfileAvatorAdapter()
        adapter.setData(displayProfile.Images, isPrefab!!, this@ProfileActivity)
        avators.apply {
            this.adapter = adapter
        }

        val name = findViewById<TextView>(R.id.profile_name)
        val momotalkstate = findViewById<TextView>(R.id.profile_momotalkstate)
        val fullname = findViewById<TextView>(R.id.profile_fullname)
        val age = findViewById<TextView>(R.id.profile_age)
        val birthday = findViewById<TextView>(R.id.profile_birthday)
        val height = findViewById<TextView>(R.id.profile_height)
        val school = findViewById<TextView>(R.id.profile_school)
        val hobbies = findViewById<TextView>(R.id.profile_hobbies)
        val description = findViewById<TextView>(R.id.profile_description)

        name.text = displayProfile.Name
        if (displayProfile.MomotalkState != null) {
            momotalkstate.text = displayProfile.MomotalkState
        }
        if (displayProfile.FirstName != null) {
            fullname.text = resources.getString(R.string.profile_name, displayProfile.FirstName + " " + displayProfile.Name)
        } else {
            fullname.text = resources.getString(R.string.profile_name, displayProfile.Name)
        }
        if (displayProfile.Age != null) {
            age.text = resources.getString(R.string.profile_age, displayProfile.Age.toString())
        }
        if (displayProfile.BirthDay != null) {
            birthday.text = resources.getString(R.string.profile_birthday, displayProfile.BirthDay!!.Month.toString(), displayProfile.BirthDay!!.Day.toString())
        }
        if (displayProfile.Height != null) {
            height.text = resources.getString(R.string.profile_height, displayProfile.Height.toString())
        }
        if (displayProfile.School != null) {
            school.text = resources.getString(R.string.profile_school, displayProfile.School.toString())
        }
        if (displayProfile.Hobbies != null) {
            hobbies.text = resources.getString(R.string.profile_hobbies, displayProfile.Hobbies!!.joinToString(Utils.SettingData.DefaultSplitChar))
        }
        if (displayProfile.Description != null) {
            description.text = displayProfile.Description
        } else {
            val image = findViewById<ImageView>(R.id.profile_downerbg)
            val title = findViewById<TextView>(R.id.profile_downertitle)
            image.visibility = View.INVISIBLE
            title.visibility = View.INVISIBLE
        }
    }

    inner class ProfileAvatorAdapter()
        : RecyclerView.Adapter<ProfileAvatorAdapter.ProfileAvatorViewHolder>() {

        var avatorList : List<Image> = listOf()
        var isPrefab : Boolean? = null
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

        fun setData(avators : List<Image>, isPrefab : Boolean, context : Context){
            avatorList = avators
            this.isPrefab = isPrefab
            this.context = context
            notifyDataSetChanged()
        }

        inner class ProfileAvatorViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
            val profileImage : ImageView

            init {
                profileImage = itemView.findViewById<ImageView>(R.id.profile_avator)
            }

            fun bindImage(avator : Image) {
                if (isPrefab!!) {
                    val input = context.assets.open(avator.ImageName + ".jpg")
                    profileImage.setImageBitmap(BitmapFactory.decodeStream(input))
                } else {
                    profileImage.setImageBitmap(BitmapFactory.decodeFile(avator.ImageOriginalUri))
                }
            }
        }
    }
}