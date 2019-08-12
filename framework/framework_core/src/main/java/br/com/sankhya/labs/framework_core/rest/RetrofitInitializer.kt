package br.com.sankhya.labs.framework_core.rest

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInitializer(url: String, timeOut: Long = 15) {
    private var retrofit: Retrofit
    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
        client.connectTimeout(timeOut, TimeUnit.SECONDS) // connect timeout
        client.readTimeout(timeOut, TimeUnit.SECONDS)    // socket timeout
        client.writeTimeout(timeOut, TimeUnit.SECONDS)
        client.addInterceptor(interceptor)

        retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(client.build())
                .addConverterFactory(
                        GsonConverterFactory.create(GsonBuilder()
                                .serializeNulls().create())
                )
                .build()
    }
    fun callRequest(): SKApiInterface = retrofit.create(SKApiInterface::class.java)
}