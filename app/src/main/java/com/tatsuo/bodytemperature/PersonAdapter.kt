package com.tatsuo.bodytemperature

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tatsuo.bodytemperature.db.Person

class PersonAdapter(val personList: List<Person>) : RecyclerView.Adapter<PersonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_person, parent, false)
        return PersonAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonAdapter.ViewHolder, position: Int) {
        holder.personId = personList[position].personId
        holder.personName.text = personList[position].name

        holder.personRadio.setOnCheckedChangeListener(null)
        if(holder.personId == ConfigManager().loadTargetPersonId()) {
            holder.personRadio.isChecked = true
        } else {
            holder.personRadio.isChecked = false
        }

        holder.personRadio.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                ConfigManager().saveTargetPersonId(holder.personId)
                ConfigManager().saveTargetPersonName(holder.personName.text.toString())
                Log.e("PersonAdapter","position : "+position+"  isChecked : "+isChecked)
                notifyDataSetChanged()
            } else {
                Log.e("PersonAdapter","position : "+position+"  isChecked : "+isChecked)
                // notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return personList.count()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var personId = 0
        val personRadio : RadioButton = view.findViewById(R.id.personRadio)
        val personName : TextView = view.findViewById(R.id.personName)
    }

}
