package br.com.sankhya.labs.framework_core.service

import br.com.sankhya.labs.framework_core.R
import br.com.sankhya.labs.framework_core.SKApplication.Companion.context
import br.com.sankhya.labs.framework_core.content.SKPreferences
import br.com.sankhya.labs.framework_core.dao.SKServerDao
import br.com.sankhya.labs.framework_core.model.SKUser
import br.com.sankhya.labs.framework_core.rest.RetrofitInitializer
import br.com.sankhya.labs.framework_core.utils.SKConstantesUtils
import br.com.sankhya.labs.framework_core.utils.SKUserUtil
import br.com.sankhya.labs.framework_core.utils.SKUtils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import java.util.*

object SKApiRequestService {

    fun sankhyaFormat(
        module: String,
        serviceName: String,
        requestBody: JsonObject = JsonObject(),
        onSuccess: ((response: JsonObject?) -> Unit) = {},
        onError: ((t: Throwable) -> Unit) = {},
        urlserver: String = "",
        timeOut: Long = 15
    ) {
        val url: String = if (urlserver.isEmpty()) {
            if (SKServerDao(context = context).getCurrentServer()?.getDomain() != null) {
                SKServerDao(context = context).getCurrentServer()?.getDomain() ?: ""
            } else {
                onError(Throwable(context.getString(R.string.server_not_configured)))
                return
            }
        } else {
            urlserver
        }


        fun getBody(): JsonObject {
            val body = JsonObject()
            body.addProperty("serviceName", serviceName)
            body.add("requestBody", requestBody)
            body.addProperty("randomUUID", UUID.randomUUID().toString())
            return body
        }

        var jsessionid: String? = null
        if (SKPreferences.contains(SKConstantesUtils.JSESSIONID)) {
            jsessionid = SKPreferences.getString(SKConstantesUtils.JSESSIONID)
        }

        val call = RetrofitInitializer(url = url, timeOut = timeOut).callRequest().callService(
            authorization = if (SKPreferences.contains(SKConstantesUtils.KID))
                "Bearer ${SKPreferences.getString(SKConstantesUtils.KID)}" else null,
            cookie = if (jsessionid != null) "JSESSIONID=$jsessionid" else null,
            module = module,
            serviceName = serviceName,
            mgeSession = jsessionid,
            aplicacao = SKUtils.getApplicationName(),
            body = getBody()
        )
        call.enqueue(object : retrofit2.Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val statusCode = response.body()?.get("status")?.asInt
                val menssageError = response.body()?.get("statusMessage")?.asString ?: response.message()
                val responseBody = response.body()?.get("responseBody")?.asJsonObject
                reloadCookie(response)
                when (statusCode) {
                    0 -> onError(Throwable(message = menssageError))
                    1 -> onSuccess(responseBody)
                    3 -> callLogout(
                        onError,
                        menssageError
                    )
                    else -> onError(Throwable(message = menssageError))
                }
                return
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t)
                return
            }
        })
    }

    private fun reloadCookie(response: Response<JsonObject>) {
        val cookies = response.headers().get("Set-Cookie")?.split(";") ?: listOf()
        for (cookie in cookies) {
            if (cookie.startsWith(SKConstantesUtils.JSESSIONID)) {
                SKPreferences.set(
                    SKConstantesUtils.JSESSIONID, cookie.replace(
                        "JSESSIONID=",
                        ""
                    ).split(".")[0]
                )
                break
            }
        }
    }

    private fun callLogout(
        onError: (t: Throwable) -> Unit,
        menssageError: String?
    ) {
        if (SKPreferences.contains(SKUserUtil.USER_PASS)) {
            val userName = SKPreferences.getString(SKUserUtil.USER_NAME) ?: ""
            val password = SKPreferences.getString(SKUserUtil.USER_PASS) ?: ""
            if(password.isEmpty()){
                onError(Throwable(message = menssageError))
                SKLoginService.doLogout()
                return
            }
            SKLoginService.doLogin(
                SKUser(
                    permissionSavesPass = true,
                    username = userName,
                    password = password
                )
            )
            onError(Throwable(message = context.getString(R.string.server_try_again)))
        } else {
            onError(Throwable(message = menssageError))
            SKLoginService.doLogout()
        }

    }


    fun isAlive(onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        sankhyaFormat(
            urlserver = "https://skwsoft01.sankhya.com.br",
            module = "mge",
            serviceName = "SystemUtilsSP.isAlive",
            onSuccess = {
                onSuccess()
            }, onError = {
                onError(it)
            })
    }

}