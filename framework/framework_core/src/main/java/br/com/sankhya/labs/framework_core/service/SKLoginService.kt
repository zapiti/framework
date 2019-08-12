package br.com.sankhya.labs.framework_core.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import br.com.sankhya.labs.framework_core.R
import br.com.sankhya.labs.framework_core.SKApplication.Companion.context
import br.com.sankhya.labs.framework_core.content.SKPreferences
import br.com.sankhya.labs.framework_core.dao.SKServerDao
import br.com.sankhya.labs.framework_core.extension.decodeBase64
import br.com.sankhya.labs.framework_core.model.SKUser
import br.com.sankhya.labs.framework_core.utils.SKAppConfigUtil
import br.com.sankhya.labs.framework_core.utils.SKConstantesUtils
import br.com.sankhya.labs.framework_core.utils.SKUserUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlin.math.roundToInt

object SKLoginService {

    fun getTastingDays(tasteStart: Long) = (
            FirebaseRemoteConfig.getInstance()
                .getLong(SKConstantesUtils.COUNT_TASTING_DAYS_LABLE).toInt()
                    - (System.currentTimeMillis() - tasteStart) / 86400000)
        .toFloat().roundToInt()

    fun doLogout(funcToCallBefore: (() -> Unit)? = null) {
        (SKAppConfigUtil.activity?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancelAll()
        val intent = Intent(SKAppConfigUtil.activity, SKAppConfigUtil.loginActivity!!::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        SKAppConfigUtil.activity?.startActivity(intent)

        funcToCallBefore?.invoke()
        if (SKAppConfigUtil.clearPreferencesInLogout) {
            SKPreferences.remove(SKUserUtil.USERID)
            SKPreferences.clear()
            SKPreferences.remove(SKConstantesUtils.AUTHENTICATED)
        }

        SKApiRequestService.sankhyaFormat(module = "mge", serviceName = "MobileLoginSP.logoutFCM")
        SKApiRequestService.sankhyaFormat(module = "mge", serviceName = "MobileLoginSP.logout")

        if (SKAppConfigUtil.activity != null) {
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener(SKAppConfigUtil.activity!!) { instanceIdResult ->
                val newToken = instanceIdResult.token
                SKPreferences.set(SKUserUtil.TOKEN, newToken)
            }
        }
    }

    fun doLogin(user: SKUser, onSuccess: (() -> Unit) = {}, onError: ((Throwable) -> Unit) = {}) {
        authenticate(user, onSuccess = {
            if (SKAppConfigUtil.activity != null) {
                FirebaseInstanceId.getInstance()
                    .instanceId.addOnSuccessListener(SKAppConfigUtil.activity!!) { instanceIdResult ->
                    val newToken = instanceIdResult.token
                    SKPreferences.set(SKUserUtil.TOKEN, newToken)
                }
            }
            getLicenses { licenses ->
                SKPreferences.remove(SKConstantesUtils.TASTING)
                when {
                    licenses == 0 -> {
                        canTaste(user) { resting ->
                            if (resting > 0) {
                                setAuthenticated(user, onSuccess)
                            } else {
                                onError(Throwable(message = context.getString(R.string.descustation_period_finish_menssage)))
                            }
                        }
                    }
                    (licenses ?: 0) > 0 -> {
                        setAuthenticated(user, onSuccess)
                    }
                    else -> {
                        setAuthenticated(user, onSuccess)
                    }
                }
            }
        }, onError = {
            onError(it)
        })
    }

    internal fun getLicenses(onCompletion: (Int?) -> Unit) {
        SKApiRequestService.sankhyaFormat(module = "mge", serviceName = "AdministracaoServidorSP.getInfo",
            onSuccess = { resp ->
                val responseBody = resp ?: JsonObject()
                SKPreferences.remove(SKConstantesUtils.COMPANY)
                SKPreferences.remove(SKConstantesUtils.COMPANY_NAME)
                SKPreferences.remove(SKConstantesUtils.LICENSES_COUNT)
                if (responseBody.has("info")) {
                    val info = responseBody.get("info").asJsonObject
                    if (info.has("sas")) {
                        val sas = info.get("sas").asJsonObject
                        if (sas.has("identificador")) {
                            val identificador = sas.get("identificador").asString
                            SKPreferences.set(SKConstantesUtils.COMPANY, identificador)
                            //todo ver esse indentificador
                            //Crashlytics.setUserIdentifier(identificador)
                            SKPreferences.set(SKConstantesUtils.COMPANY_NAME, sas.get("clientName").asString)


                            val db = FirebaseFirestore.getInstance()
                            var licenses = 0
                            SKPreferences.set(SKConstantesUtils.LICENSES_UPDATED, System.currentTimeMillis())
                            if (SKAppConfigUtil.ProductCode == null) {
                                licenses = 2147483647
                            } else {
                                val grupo = sas.get("grupo")
                                val grupos: JsonArray
                                if (grupo.isJsonArray) {
                                    grupos = grupo.asJsonArray
                                } else {
                                    grupos = JsonArray()
                                    grupos.add(grupo.asJsonObject)
                                }
                                for (g in grupos) {
                                    val product = g.asJsonObject.get("info")
                                    val products: JsonArray
                                    if (product.isJsonArray) {
                                        products = product.asJsonArray
                                    } else {
                                        products = JsonArray()
                                        products.add(product.asJsonObject)
                                    }
                                    for (p in products) {
                                        if (p.asJsonObject.get("COD").asString == SKAppConfigUtil.ProductCode) {
                                            licenses = Integer.parseInt(g.asJsonObject.get("LICENCAS").asString)
                                        }
                                    }
                                }
                            }
                            val company = db.collection("companies").document(identificador)
                            company.set(
                                mapOf(

                                    Pair("code", sas.get("clientId").asString),
                                    Pair("name", sas.get("clientName").asString),
                                    Pair("database", info.get("db").asJsonObject.get("sgdbName").asString),
                                    Pair("dbVersion", info.get("db").asJsonObject.get("driverVersion").asString),
                                    Pair("version", info.get("sankhyaW").asJsonObject.get("version").asString),
                                    Pair("licenses", licenses),
                                    Pair("erp", (if (SKAppConfigUtil.isJIVA) "Jiva" else "Sankhya") + "-W")
                                ), SetOptions.merge()
                            )
                            SKPreferences.set(SKConstantesUtils.CLIENTID, sas.get("clientId").asString)
                            SKPreferences.set(SKConstantesUtils.LICENSES_COUNT, licenses)
                            SKPreferences.set(SKUserUtil.CODEMP, sas.get("clientId").asString)
                            SKPreferences.set(SKUserUtil.NAMEMP, sas.get("clientName").asString)
                            onCompletion(licenses)
                            return@sankhyaFormat
                        }
                        onCompletion(-1)
                        return@sankhyaFormat
                    }
                }
            }, onError = {
                onCompletion(null)
            })
    }


    private fun canTaste(user: SKUser, onCompletion: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("companies")
            .document(SKPreferences.getString(SKConstantesUtils.COMPANY) ?: "")
            .get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    if (task.result != null && task.result!!.exists()) {
                        val tasteStartTime = task.result!!.getDate("tasteStartTime")
                        if (tasteStartTime != null) {
                            val resting = getTastingDays(tasteStartTime.time)
                            if (resting > 0) {
                                SKPreferences.set(SKConstantesUtils.TASTING, tasteStartTime.time)
                            }
                            onCompletion(resting)
                        } else {
                            db.collection("companies").document(
                                SKPreferences
                                    .getString(SKConstantesUtils.COMPANY)!!
                            ).set(
                                mapOf(
                                    Pair("tasteStartUser", user.username),
                                    Pair("tasteStartTime", FieldValue.serverTimestamp())
                                ), SetOptions.merge()
                            )
                            val remoteConfig = FirebaseRemoteConfig.getInstance()
                                .getLong(SKConstantesUtils.COUNT_TASTING_DAYS_LABLE).toInt()
                            onCompletion(remoteConfig)
                        }
                    }
                }
            }


    }


    private fun setAuthenticated(user: SKUser, onSucess: () -> Unit) {
        SKPreferences.set(SKConstantesUtils.AUTHENTICATED, true)
        SKPreferences.remove(SKUserUtil.USER_IMAGE)
        SKPreferences.set(
            SKUserUtil.USER_IMAGE, "${SKServerDao(context)
                .getCurrentServer()?.getDomain()}/mge/image/user/${user.codusu}.png?w=200&h=200"
        )
        onSucess()
        postLog()
    }


    private fun authenticate(user: SKUser, onSuccess: (() -> Unit) = {}, onError: ((Throwable) -> Unit) = {}) {
        SKPreferences.remove(SKUserUtil.USERID)
        SKPreferences.remove(SKUserUtil.USER_NAME)
        SKPreferences.remove(SKUserUtil.USER_PASS)
        SKPreferences.remove(SKConstantesUtils.KID)
        SKPreferences.remove(SKConstantesUtils.JSESSIONID)
        val nomusu = JsonObject()
        nomusu.addProperty("$", user.username)
        val interno = JsonObject()
        interno.addProperty("$", user.password)
        val keepConnected = JsonObject()
        keepConnected.addProperty("$", "S")
        val appName = JsonObject()
        appName.addProperty("$", SKAppConfigUtil.ProductCode ?: SKConstantesUtils.FREE)
        if (SKAppConfigUtil.ProductCodePush != null) {
            appName.addProperty("$", SKAppConfigUtil.ProductCodePush)
        }
        val aparelho = JsonObject()
        aparelho.addProperty("$", "android")
        val aparelhoId = JsonObject()
        aparelhoId.addProperty("$", SKPreferences.getString(SKUserUtil.TOKEN) ?: "")
        val requestBody = JsonObject()
        requestBody.add("NOMUSU", nomusu)
        requestBody.add("INTERNO", interno)
        requestBody.add("KEEPCONNECTED", keepConnected)
        requestBody.add("APPNAME", appName)
        requestBody.add("APARELHO", aparelho)
        requestBody.add("APARELHO_ID", aparelhoId)
        SKApiRequestService.sankhyaFormat(
            module = "mge",
            serviceName = "MobileLoginSP.login",
            requestBody = requestBody,
            onSuccess = { responseBody ->
                SKPreferences.set(
                    SKConstantesUtils.KID, responseBody?.get("kID")
                        ?.asJsonObject?.get("$")?.asString?.decodeBase64()
                )
                user.codusu = responseBody?.get("idusu")?.asJsonObject?.get("$")?.asString?.decodeBase64()
                SKPreferences.set(SKUserUtil.USERID, user.codusu.toString())
                SKPreferences.set(SKUserUtil.USER_NAME, user.username)
                if (user.permissionSavesPass) {
                    SKPreferences.set(SKUserUtil.USER_PASS, user.password)
                }
                //todo ver isso
                //Crashlytics.setUserName(user.username)
                if (SKPreferences.contains(SKUserUtil.TOKEN)) {
                    SKPreferences.set(SKUserUtil.TOKENVALID, true)
                }
                //todo ver isso

//            getUserInfo{
//                return@getUserInfo
//            }
                onSuccess()
            }, onError = {
                onError(it)
            })


    }


    private fun postLog(eventId: String? = null) {
        if (SKPreferences.getBoolean(SKConstantesUtils.AUTHENTICATED)) {
            val companyId = SKPreferences.getString(SKConstantesUtils.COMPANY)
            if (companyId != null) {
                val log = mutableMapOf<String, Any>()
                log["brand"] = android.os.Build.BRAND
                log["codUsu"] = SKPreferences.getString(SKUserUtil.USERID) ?: ""
                log["date"] = FieldValue.serverTimestamp()
                log["domain"] = SKServerDao(context).getCurrentServer()?.getDomain() ?: ""
                log["model"] = android.os.Build.MODEL
                log["nomeUsu"] = SKPreferences.getString(SKUserUtil.USER_NAME) ?: ""
                log["token"] = SKPreferences.getString(SKUserUtil.TOKEN) ?: ""
                log["version"] = SKAppConfigUtil.AppVersion ?: ""
                if (eventId != null) {
                    log["eventId"] = eventId
                }
                SKPreferences.set(SKUserUtil.DOMAIN, log["domain"].toString())
                SKPreferences.set(SKUserUtil.DATEACESS, log["date"].toString())
                FirebaseFirestore.getInstance().collection("companies")
                    .document(companyId)
                    .collection(if (eventId == null) "defaultLogs" else "eventLogs").document().set(log)
            }
        }
    }
}