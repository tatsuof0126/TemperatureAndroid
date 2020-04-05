package com.tatsuo.bodytemperature

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.tatsuo.bodytemperature.db.Temperature
import java.util.*

class GraphView : View {
    private var paint: Paint = Paint()

    var targetDate : Date? = null
    var temperatureList : List<Temperature> = listOf()
    var graphType : Int = 1

    init {

    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

    }

    override fun onDraw(canvas: Canvas){
        val wRate : Float = width / 100f
        val hRate : Float = height / 100f

        val useFahrenheit = ConfigManager.loadUseFahrenheitFlag()

        // Log.e("***Temperature***", "Width : "+width+" Height : "+height)

        paint.color = Color.argb(255, 0, 0, 0)
        paint.style = Paint.Style.STROKE

        // paint.strokeWidth = 4f
        paint.strokeWidth = 0.4f*wRate
        canvas.drawLine(10f*wRate, 0f*hRate, 10f*wRate, 100f*hRate, paint)

        val baseY = if(useFahrenheit) 70f else 78f

        canvas.drawLine(0f*wRate, baseY*hRate, 100f*wRate, baseY*hRate, paint)

        // canvas.drawLine(0f*wRate, 78f*hRate, 100f*wRate, 78f*hRate, paint)
        // canvas.drawLine(0f*wRate, 70f*hRate, 100f*wRate, 70f*hRate, paint)

        paint.strokeWidth = 1f
        if(useFahrenheit){
            canvas.drawLine(0f*wRate, 10f*hRate, 100f*wRate, 10f*hRate, paint)
            canvas.drawLine(0f*wRate, 30f*hRate, 100f*wRate, 30f*hRate, paint)
            canvas.drawLine(0f*wRate, 50f*hRate, 100f*wRate, 50f*hRate, paint)
            canvas.drawLine(0f*wRate, 90f*hRate, 100f*wRate, 90f*hRate, paint)
        } else {
            canvas.drawLine(0f * wRate, 6f * hRate, 100f * wRate, 6f * hRate, paint)
            canvas.drawLine(0f * wRate, 24f * hRate, 100f * wRate, 24f * hRate, paint)
            canvas.drawLine(0f * wRate, 42f * hRate, 100f * wRate, 42f * hRate, paint)
            canvas.drawLine(0f * wRate, 60f * hRate, 100f * wRate, 60f * hRate, paint)
            canvas.drawLine(0f * wRate, 96f * hRate, 100f * wRate, 96f * hRate, paint)
        }

        paint.strokeWidth = 0.3f*wRate
        if(graphType == 1) {
            // 3日間グラフ
            canvas.drawLine(14f * wRate, (baseY-4f)*hRate, 14f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(40f * wRate, (baseY-4f)*hRate, 40f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(66f * wRate, (baseY-4f)*hRate, 66f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(92f * wRate, (baseY-4f)*hRate, 92f * wRate, (baseY+4f)*hRate, paint)
        } else if(graphType == 2){
            // 7日間グラフ
            canvas.drawLine(13f * wRate, (baseY-4f)*hRate, 13f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(25f * wRate, (baseY-4f)*hRate, 25f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(37f * wRate, (baseY-4f)*hRate, 37f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(49f * wRate, (baseY-4f)*hRate, 49f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(61f * wRate, (baseY-4f)*hRate, 61f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(73f * wRate, (baseY-4f)*hRate, 73f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(85f * wRate, (baseY-4f)*hRate, 85f * wRate, (baseY+4f)*hRate, paint)
            canvas.drawLine(97f * wRate, (baseY-4f)*hRate, 97f * wRate, (baseY+4f)*hRate, paint)
        }

        paint.style = Paint.Style.FILL
        paint.strokeWidth = 2f
        paint.textSize = 3.7f*wRate
        // paint.textSize = 40f
        if(useFahrenheit){
            canvas.drawText("104.0", 0f*wRate,9f*hRate, paint)
            canvas.drawText("102.0", 0f*wRate,29f*hRate, paint)
            canvas.drawText("100.0", 0f*wRate,49f*hRate, paint)
            canvas.drawText("98.0", 2f*wRate,69f*hRate, paint)
            canvas.drawText("96.0", 2f*wRate,89f*hRate, paint)
        } else {
            canvas.drawText("40.0", 2f * wRate, 5f * hRate, paint)
            canvas.drawText("39.0", 2f * wRate, 23f * hRate, paint)
            canvas.drawText("38.0", 2f * wRate, 41f * hRate, paint)
            canvas.drawText("37.0", 2f * wRate, 59f * hRate, paint)
            canvas.drawText("36.0", 2f * wRate, 77f * hRate, paint)
            canvas.drawText("35.0", 2f * wRate, 95f * hRate, paint)
        }

        if(targetDate != null) {
            val cal = Calendar.getInstance()
            cal.time = targetDate

            if(graphType == 1) {
                val str3: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str2: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str1: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()

                canvas.drawText(str1, (23f-getAdjustX(str1)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str2, (49f-getAdjustX(str2)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str3, (75f-getAdjustX(str3)) * wRate, (baseY+4f)*hRate, paint)
            } else if(graphType == 2){
                val str7: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str6: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str5: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str4: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str3: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str2: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()
                cal.add(Calendar.DATE, -1)
                val str1: String = (cal.get(Calendar.MONTH) + 1).toString() + "/" + cal.get(Calendar.DAY_OF_MONTH).toString()

                canvas.drawText(str1, (16f-getAdjustX(str1)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str2, (28f-getAdjustX(str2)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str3, (40f-getAdjustX(str3)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str4, (52f-getAdjustX(str4)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str5, (64f-getAdjustX(str5)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str6, (76f-getAdjustX(str6)) * wRate, (baseY+4f)*hRate, paint)
                canvas.drawText(str7, (88f-getAdjustX(str7)) * wRate, (baseY+4f)*hRate, paint)
            }
        }

        var oldX = 0f
        var oldY = 0f
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0.3f*wRate
        for (temperature in temperatureList) {
            var tempY = 0f
            if(useFahrenheit){
                tempY = ((100f - temperature.getFahrenheitTemperature()) * 10f + 50).toFloat()
            } else {
                tempY = ((40f - temperature.temperature) * 18f + 6).toFloat()
            }

            if(tempY > 100f){
                tempY = 100f
            }
            if(tempY < 0f){
                tempY = 0f
            }

            val cal = Calendar.getInstance()
            cal.time = targetDate
            if(graphType == 1){
                cal.add(Calendar.DATE, 1-3)
            } else if(graphType == 2){
                cal.add(Calendar.DATE, 1-7)
            }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val beforDate = cal.time

            var tempX = 0f
            if(graphType == 1){
                tempX = (temperature.date.time - beforDate.time) * 78f / (1000f * 60 * 60 * 24 * 3) + 14f
            } else if(graphType == 2){
                tempX = (temperature.date.time - beforDate.time) * 84f / (1000f * 60 * 60 * 24 * 7) + 13f
            }

            paint.color = Color.argb(255, 255, 0, 0)
            canvas.drawCircle(tempX*wRate, tempY*hRate, 10f, paint)

            if(oldX != 0f){
                paint.color = Color.argb(255, 255, 0, 0)
                canvas.drawLine(tempX*wRate, tempY*hRate, oldX*wRate, oldY*hRate, paint)
            }
            oldX = tempX
            oldY = tempY
        }

    }

    private fun getAdjustX(string : String) : Float {
        if(string.length == 4){
            return 1f
        } else if(string.length == 5){
            return 2f
        }
        return 0f
    }

}
