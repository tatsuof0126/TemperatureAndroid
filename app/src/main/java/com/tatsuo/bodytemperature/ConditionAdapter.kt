package com.tatsuo.bodytemperature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.tatsuo.bodytemperature.db.ConditionList

class ConditionAdapter(val selectedConditionIdList : MutableList<Int> = mutableListOf<Int>()) :
        RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_condition, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var exists = false
        for(condition in selectedConditionIdList){
            if(condition == position){
                exists = true
            }
        }

        holder.selectedCondition.isChecked = exists
        holder.selectedCondition.text = ConditionList.conditionList[position].text
        holder.selectedCondition.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                selectedConditionIdList.add(position)
            } else {
                selectedConditionIdList.remove(position)
            }

            /*
            // テストコード
            Log.e("ConditionAdapter","position : "+position+"  isChecked : "+isChecked)
            var tempStr = ""
            for(idInt in selectedConditionIdList){
                tempStr += ""+idInt+","
            }
            Log.e("ConditionAdapter","selectedConditionId : "+tempStr)
            */
        }
    }

    override fun getItemCount(): Int {
        return ConditionList.conditionList.count()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val selectedCondition: CheckBox = view.findViewById(R.id.selectCondition)
    }
}

