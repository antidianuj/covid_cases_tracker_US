package com.example.covid_record_tracking

import android.graphics.RectF
import com.robinhood.spark.SparkAdapter

class CovidSparkAdapter(private val dailydata: List<CovidData>):SparkAdapter() {
   var metric=Metric.POSITIVE
    var daysAgo=TimeScale.MAX


    override fun getCount()=dailydata.size

    override fun getItem(index: Int)=dailydata[index]

    override fun getY(index: Int): Float {
        val chosenDayData=dailydata[index]
        return when(metric)
        {
            Metric.NEGATIVE->chosenDayData.negativeIncrease.toFloat()
            Metric.POSITIVE->chosenDayData.positiveIncrease.toFloat()
            Metric.DEATH->chosenDayData.deathIncrease.toFloat()
        }
        return chosenDayData.positiveIncrease.toFloat()
    }

    override fun getDataBounds(): RectF {
        val bounds= super.getDataBounds()
        if (daysAgo!=TimeScale.MAX)
        {
        bounds.left=count-daysAgo.numDays.toFloat()}
        return bounds
    }

}
