package com.tatsuo.temperature

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectConditionActivity : AppCompatActivity() {

    var selectConditionIdList : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_condition)

        setTitle("症状を選択")
        setResult(Activity.RESULT_CANCELED)

        val conditions = intent.getStringExtra("CONDITIONS")
        selectConditionIdList = getConditionIdList(conditions).toMutableList()

        /*
        // val selectConditionList : ArrayList<Int> = ArrayList<Int>()
        val selConArray = conditions.split(",")
        for(conditionIdStr in selConArray){
            try {
                val conditionId = conditionIdStr.toInt()
                selectConditionList.add(conditionId)
            } catch (nfe : NumberFormatException ){}
        }
        */

        val adapter = ConditionAdapter(selectConditionIdList)
        val conditionRecyclerView : RecyclerView = findViewById(R.id.conditionRecyclerView)
        conditionRecyclerView.adapter = adapter
        conditionRecyclerView.layoutManager = LinearLayoutManager(this)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_select_condition, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveCondition -> {
                val retString = StringBuilder()
                for(conditionId in selectConditionIdList.sorted()){
                    retString.append(conditionId.toString())
                    retString.append(",")
                }
                if(retString.length >= 1) {
                    retString.delete(retString.length-1, retString.length)
                }

                val result = Intent()
                result.putExtra("CONDITIONS", retString.toString())
                setResult(Activity.RESULT_OK, result)
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

}
