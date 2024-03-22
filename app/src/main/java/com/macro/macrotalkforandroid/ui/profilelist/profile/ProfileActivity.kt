package com.macro.macrotalkforandroid.ui.profilelist.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.util.TextInfo
import com.macro.macrotalkforandroid.Conversation
import com.macro.macrotalkforandroid.Image
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.ProfileSelector
import com.macro.macrotalkforandroid.R
import com.macro.macrotalkforandroid.Utils
import com.macro.macrotalkforandroid.ui.conversationlist.conversation.ConversationActivity
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.math.ceil
import kotlin.math.sqrt

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

        val button = findViewById<Button>(R.id.startConversationButton)
        button.setOnClickListener(OnStartConversationClick())
    }

    inner class OnStartConversationClick() : OnClickListener {
        override fun onClick(v: View?) {
            val view = v!!
            val title = view.findViewById<EditText>(R.id.add_conversation_title)
            val profile = displayProfile
            val tags = view.findViewById<EditText>(R.id.add_conversation_tags)

            val titleText = profile.Name

            val context = this@ProfileActivity
            val gridLayout = GridLayout(context)
            val gridWidth = 1
            gridLayout.columnCount = gridWidth
            val image = ImageView(context)
            if (!profile.Images[0].isNotPrefab) {
                image.setImageBitmap(BitmapFactory.decodeStream(context.assets.open(profile.Images[0].ImageName + ".jpg")))
            } else {
                image.setImageBitmap(BitmapFactory.decodeFile(profile.Images[0].ImageOriginalUri))
            }
            val params = LinearLayout.LayoutParams(
                Utils.dip2px(context, 40f),
                Utils.dip2px(context, 40f)
            )
            image.layoutParams = params
            gridLayout.addView(image)

            gridLayout.setBackgroundColor(Color.WHITE)
            gridLayout.layout(0, 0, gridWidth * Utils.dip2px(
                context,
                40f
            ), gridWidth * Utils.dip2px(context, 40f)
            )
            val measuredWidth =
                View.MeasureSpec.makeMeasureSpec(gridWidth * Utils.dip2px(context, 40f), View.MeasureSpec.EXACTLY)
            val measuredHeight =
                View.MeasureSpec.makeMeasureSpec(gridWidth * Utils.dip2px(context, 40f), View.MeasureSpec.EXACTLY)
            gridLayout.measure(measuredWidth, measuredHeight)
            gridLayout.layout(0, 0, gridLayout.measuredWidth, gridLayout.measuredHeight)

            val viewWidth = gridLayout.measuredWidth
            val viewHeight = gridLayout.measuredHeight
            val bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
            val cvs = Canvas(bitmap)
            gridLayout.draw(cvs)

            val saveFile = File(Utils.appDataPath, titleText)
            try {
                val saveImgOut = FileOutputStream(saveFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut)
                saveImgOut.flush()
                saveImgOut.close()
            } catch (_ : Exception) {}
            val image1 = Image(titleText + "_Cover", Utils.appDataPath + "/" + titleText, true)

            val conversation = Conversation(
                titleText,
                listOf(profile.toProfileSelector()),
                image1,
                null
            )
            Utils.storageData.Conversations.add(conversation)
            ConversationActivity.conversation = conversation
            val intent = Intent(this@ProfileActivity, ConversationActivity()::class.java)
            startActivity(intent)
        }
    }
}