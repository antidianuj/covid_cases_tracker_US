package com.example.covid_record_tracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.robinhood.spark.SparkAdapter


private const val BASE_URL="https://covidtracking.com/api/v1/"
private const val TAG="MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var adapter: CovidSparkAdapter
    private lateinit var perStateDailyData: Map<Unit, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson= GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit= Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covidService=retrofit.create(CovidService::class.java)



        //Fetch National Data
        covidService.getNationalData().enqueue(object: Callback<List<CovidData>>{
            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG,"onFailure $t")
            }

            override fun onResponse(call: Call<List<CovidData>>,response:Response<List<CovidData>>) {
                Log.i(TAG,"onResponse $response")
                val nationalData=response.body()
                if (nationalData==null)
                {
                    Log.e(TAG,"Did not receive a valid response body")
                    return
                }
                setupEventListers()
                nationalDailyData=nationalData.reversed()
                Log.i(TAG,"Update Graph with national data")
                updateDisplayWithData(nationalDailyData)
            }
        })

        //Fetch State Data
        covidService.getStatesData().enqueue(object: Callback<List<CovidData>>{
            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG,"onFailure $t")
            }

            override fun onResponse(call: Call<List<CovidData>>,response:Response<List<CovidData>>) {
                Log.i(TAG,"onResponse $response")
                val statesData=response.body()
                if (statesData==null)
                {
                    Log.e(TAG,"Did not receive a valid response body")
                    return
                }
                perStateDailyData=statesData.reversed().groupBy { it.state }
                Log.i(TAG,"Update spinner with state names")
            }
        })
    }

    private fun setupEventListers() {
        ///Add a listener for chart
        SparkView.isScrubEnabled=true
        SparkView.setScrubListener { itemData ->
            if (itemData is CovidData)
            {

               updateInfoForDate(itemData)
            }
        }
        //Respond to radio buttons
        Groupselect2.setOnCheckedChangeListener{ _, checkedId ->
        adapter.daysAgo=when (checkedId)
        {
            R.id.radioButtonWeek->TimeScale.WEEK
            R.id.radioButtonMonth->TimeScale.MONTH
            else->TimeScale.MAX

        }
            adapter.notifyDataSetChanged()
        }
        Groupselect.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId)
            {
                R.id.radioButtonPositive->updateDisplayMetric(Metric.POSITIVE)
                R.id.radioButtonNegative->updateDisplayMetric(Metric.NEGATIVE)
                R.id.radioButtonDeath->updateDisplayMetric(Metric.DEATH)
            }
        }
    }

    private fun updateDisplayMetric(metric: Metric) {
        adapter.metric=metric
        adapter.notifyDataSetChanged()
    }

    private fun updateDisplayWithData(dailydata: List<CovidData>) {
        //Create SparkAdapter with data
        adapter=CovidSparkAdapter(dailydata)
        SparkView.adapter=adapter
        //Update radio buttons
        radioButtonPositive.isChecked=true
        radioButtonMax.isChecked=true
        //Display metric with recent date
        updateInfoForDate(dailydata.last())

    }

    private fun updateInfoForDate(covidData: CovidData) {
        val numCases=when(adapter.metric)
        {
            Metric.NEGATIVE->covidData.negativeIncrease
            Metric.POSITIVE->covidData.positiveIncrease
            Metric.DEATH->covidData.deathIncrease
        }
        tvMetricLabel.text=NumberFormat.getInstance().format(numCases)
        val outputDateFormat=SimpleDateFormat("MMM dd, yyyy", Locale.US)
        tvDateLabel.text=outputDateFormat.format(covidData.dateChecked)
    }
}