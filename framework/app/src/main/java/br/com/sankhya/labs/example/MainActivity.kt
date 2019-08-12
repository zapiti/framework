package br.com.sankhya.labs.example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.com.sankhya.labs.framework_core.content.SKPreferences
import br.com.sankhya.labs.framework_core.controller.SKAppCompatActivity
import br.com.sankhya.labs.framework_core.controller.SKSplashScream
import br.com.sankhya.labs.framework_core.model.SKUser
import br.com.sankhya.labs.framework_core.service.SKApiRequestService
import br.com.sankhya.labs.framework_core.service.SKLoginService
import br.com.sankhya.labs.framework_core.utils.SKUserUtil
import com.google.gson.JsonObject

class MainActivity : SKAppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
