package com.macro.macrotalkforandroid.ui.conversationlist

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.macro.macrotalkforandroid.Profile
import com.macro.macrotalkforandroid.R


class AddConversationProfileAdapter(private val context: Context, var profileList: List<Profile>) :
    RecyclerView.Adapter<AddConversationProfileAdapter.AddConversationProfileViewHodler>(), Filterable {
    val sourceProfileList = profileList

    var selectedProfiles : List<Profile> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddConversationProfileViewHodler {
        val itemView = LayoutInflater.from(context).inflate(R.layout.add_conversation_profile_item, parent, false)
        return AddConversationProfileViewHodler(itemView)
    }

    override fun onBindViewHolder(holder: AddConversationProfileViewHodler, position: Int) {
        val data: Profile = profileList[position]
        holder.profileIsCheck.setOnCheckedChangeListener(AddConversationProfileCheckChanged(position))
        if (!data.Images[0].isNotPrefab) {
            val input = context.assets.open(data.Images[0].ImageName + ".jpg")
            holder.profileImage.setImageBitmap(BitmapFactory.decodeStream(input))
        } else {
            val bitmap = BitmapFactory.decodeFile(data.Images[0].ImageOriginalUri)
            holder.profileImage.setImageBitmap(bitmap)
        }
        holder.profileName.text = data.Name
        holder.profileIsCheck.isChecked = selectedProfiles.contains(profileList[position])
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filterDatas : List<Profile>
                if (charString.isEmpty()) {
                    filterDatas = sourceProfileList
                } else {
                    val filteredList: MutableList<Profile> = ArrayList()
                    for (i in 0 until sourceProfileList.size) {
                        if (sourceProfileList[i].Name.contains(charString)) {
                            filteredList.add(sourceProfileList[i])
                        }
                    }
                    filterDatas = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterDatas
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                profileList = filterResults.values as List<Profile>
                notifyDataSetChanged()
            }
        }
    }

    override fun onViewAttachedToWindow(holder: AddConversationProfileViewHodler) {
        super.onViewAttachedToWindow(holder)

        val animation = AnimationUtils.loadAnimation(holder.view.context, R.anim.list_item_anim)
        holder.view.startAnimation(animation)
    }

    inner class AddConversationProfileViewHodler(itemView : View) : ViewHolder(itemView) {
        val profileImage : ImageView
        val profileName : TextView
        val profileIsCheck : CheckBox

        val view : View

        init {
            profileImage = itemView.findViewById<View>(R.id.profile_Image) as ImageView
            profileName = itemView.findViewById<View>(R.id.profile_Name) as TextView
            profileIsCheck = itemView.findViewById<View>(R.id.profile_isChecked) as CheckBox

            view = itemView
        }
    }

    inner class AddConversationProfileCheckChanged(val index : Int) : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (isChecked) {
                selectedProfiles += profileList[index]
            } else {
                selectedProfiles -= profileList[index]
            }
        }
    }
}