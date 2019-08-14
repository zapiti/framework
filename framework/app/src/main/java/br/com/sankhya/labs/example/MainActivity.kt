package br.com.sankhya.labs.example

import android.os.Bundle
import br.com.sankhya.labs.framework_core.content.SKPreferences
import br.com.sankhya.labs.framework_core.controller.SKAppCompatActivity
import br.com.sankhya.labs.framework_core.extension.decodeBase64
import br.com.sankhya.labs.framework_core.model.SKUser
import br.com.sankhya.labs.framework_core.service.SKApiRequestService
import br.com.sankhya.labs.framework_core.utils.SKConstantesUtils
import br.com.sankhya.labs.framework_core.utils.SKUserUtil
import com.google.gson.GsonBuilder
import java.util.HashMap

class MainActivity : SKAppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        autenticateFolinha(SKUser("NATHAN.OLIVEIRA", "123456ab"))


//       SKLoginService.doLogin(SKUser("SUP",""),onSuccess = {
//           val requestBody = JsonObject()
//           requestBody.addProperty(
//               "sql",
//               "SELECT EMAIL FROM TSIUSU " +
//                       "WHERE CODUSU = ${SKPreferences.getString(SKUserUtil.USERID)}"
//           )
//           SKApiRequestService.sankhyaFormat(
//               module = "mge",
//               requestBody = requestBody,
//               serviceName = "DbExplorerSP.executeQuery",
//               onSuccess = {response ->
//                   val trd = response
//                   Log.d("rabanada","sucessso")
//               },onError = {tward->
//                   val tt =     tward
//                   Log.d("rabanada",tward.message.toString())
//
//               })
//        },onError = {tward->
//           Log.d("rabanada",tward.message.toString())
//           val tt =     tward
//        })
//


//        SKApiRequestService.isAlive {
//
//        }
    }

    fun autenticateFolinha(user: SKUser, onSuccess: (() -> Unit) = {}, onError: ((Throwable) -> Unit) = {}) {
        val attributes = HashMap<String, Any?>()
        attributes["usuario"] = user.username
        attributes["senha"] = user.password
        val gsonBuilder = GsonBuilder()
        gsonBuilder.serializeNulls()
        val gson = gsonBuilder.create()
        val requestBody = gson.toJsonTree(attributes).asJsonObject
        SKApiRequestService.sankhyaFormat(
            urlserver = "https://skw.sankhya.com.br",
            module = "mgepes",
            serviceName = "PortalRhMaisLoginSP.login",
            requestBody = requestBody,
            onSuccess = { responseBody ->
                val userValide = responseBody?.get("loginValido")?.asBoolean ?: false
                if (userValide) {
                    //todo faça algum login
                  val sessao = responseBody?.get("sessao")?.asString ?: ""
                    onSuccess()
                } else {
                    onError(Throwable(message = getString(R.string.user_or_pass_incorret)))
                }
                SKPreferences.set(
                    SKConstantesUtils.KID, responseBody?.get("kID")
                        ?.asJsonObject?.get("$")?.asString?.decodeBase64()
                )
            }, onError = {
                onError(it)
            })


    }
}
