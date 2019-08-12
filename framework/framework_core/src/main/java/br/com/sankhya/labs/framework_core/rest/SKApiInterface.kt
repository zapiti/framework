package br.com.sankhya.labs.framework_core.rest

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface SKApiInterface {

    @POST("/{module}/service.sbr?outputType=json")
    fun callService(@Header("Authorization") authorization: String?,
                    @Header("Cookie") cookie: String?,
                    @Path("module") module: String,
                    @Query("serviceName") serviceName: String,
                    @Query("application") aplicacao: String,
                    @Query("mgeSession") mgeSession: String?,
                    @Body body: JsonObject): Call<JsonObject>

}