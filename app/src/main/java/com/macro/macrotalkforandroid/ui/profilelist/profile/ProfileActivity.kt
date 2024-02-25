package com.macro.macrotalkforandroid.ui.profilelist.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils

class ProfileActivity() : AppCompatActivity() {
    companion object {
        lateinit var displayProfile : Profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setTitle(R.string.profile_title)

        val avators = findViewById<ViewPager2>(R.id.profile_avators)
        val adapter = ProfileAvatorAdapter()
        adapter.context = this@ProfileActivity
        adapter.setData(displayProfile.Images)
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
            hobbies.text = resources.getString(R.string.profile_hobbies, displayProfile.Hobbies!!.joinToString(
                Utils.SettingData.DefaultSplitChar))
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
}