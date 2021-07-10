package com.example.covid_record_tracking

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CovidService {
    @GET("us/daily.json")
    fun getNationalData():Call<List<CovidData>>
    @GET("states/daily.json")
    fun getStatesData():Call<List<CovidData>>



}