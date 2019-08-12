package br.com.sankhya.labs.framework_core.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import br.com.sankhya.labs.framework_core.content.SKPreferences
import br.com.sankhya.labs.framework_core.dao.SKServerDao
import br.com.sankhya.labs.framework_core.service.SKLoginService
import br.com.sankhya.labs.framework_core.utils.SKAppConfigUtil
import br.com.sankhya.labs.framework_core.utils.SKConstantesUtils
import br.com.sankhya.labs.framework_core.utils.SKUserUtil
import br.com.sankhya.labs.framework_core.utils.SKUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.jetbrains.anko.doAsync


abstract class SKSplashScream : SKAppCompatActivity() {

    private lateinit var mFirebaseFirestore: FirebaseFirestore
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SKAppConfigUtil.splashscreenTheme)

        initData()
    }

    private fun initData() {
        mFirebaseFirestore = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()

        if (SKAppConfigUtil.ProductCode != null &&
            SKPreferences.contains(SKUserUtil.USERID) &&
            SKUtils.isNetworkAvailable()
        ) {
            SKLoginService.getLicenses {
                initializeTokenFirebase()
            }
        } else {
            initializeTokenFirebase()
        }

    }

    private fun initializeTokenFirebase() {


        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this)
        { instanceIdResult ->
            val newToken = instanceIdResult.token
            SKPreferences.set(SKUserUtil.TOKEN, newToken)
            initFirebase(newToken)
        }

        FirebaseInstanceId.getInstance().instanceId.addOnFailureListener {
            if (SKUtils.isNetworkAvailable()) {
                errorMenssage()
            } else {
                errorNetworkMenssage()
            }
        }
    }

    private fun initFirebase(newToken: String) {

        doAsync {

            //region <! Caso tenha um usuario corrente executar Launch !>
            if (mFirebaseAuth.currentUser != null) {
                setupFirebaseAndLaunch(newToken, mFirebaseAuth, mFirebaseFirestore)
                return@doAsync
            }
            //endregion

            mFirebaseAuth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setupFirebaseAndLaunch(newToken, mFirebaseAuth, mFirebaseFirestore)
                } else {
                    Log.d("<FW> SplashActivity", "FirebaseAuth signInAnonymously error")
                    runOnUiThread {
                        initData()
                    }
                }
            }
            return@doAsync
        }
    }

    private fun setupFirebaseAndLaunch(
        newToken: String,
        mFirebaseAuth: FirebaseAuth,
        mFirebaseFirestore: FirebaseFirestore
    ) {
        Log.d("<FW> SplashActivity", "FirebaseInstanceId token: $newToken")
        Log.d("<FW> SplashActivity", "FirebaseAuth currentUser: ${mFirebaseAuth.currentUser?.uid}")
        mFirebaseFirestore.collection("temp").document(newToken).addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null && snapshot.exists()) {
                if (snapshot.getDate("time") != null) {
                    mFirebaseFirestore.collection("temp").document(newToken).delete()
                }
            }
        }
        mFirebaseFirestore.collection("temp").document(newToken)
            .set(mapOf(Pair("time", FieldValue.serverTimestamp())), SetOptions.merge())
        FirebaseRemoteConfig.getInstance()
            .setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().setFetchTimeoutInSeconds(3).build())

        FirebaseRemoteConfig.getInstance().fetch(0).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseRemoteConfig.getInstance().fetchAndActivate()
            } else {
                FirebaseRemoteConfig.getInstance().setDefaults(
                    mapOf(
                        Pair(SKConstantesUtils.COUNT_TASTING_DAYS_LABLE, SKConstantesUtils.COUNT_TASTING_DAYS)
                    )
                )
                FirebaseRemoteConfig.getInstance().fetch(0).addOnCompleteListener { tasks ->
                    if (tasks.isSuccessful) {
                        FirebaseRemoteConfig.getInstance().fetchAndActivate()
                    }
                }
            }
        }
        callNextPage()
    }

    private fun callNextPage() {
        val serverDao = SKServerDao(this)
        if (serverDao.getServerCount() > 0 && SKAppConfigUtil.FixedServer != null) {
            val currentServer = serverDao.getCurrentServer()
            if (currentServer?.getDomain() != SKAppConfigUtil.FixedServer?.getDomain()) {
                serverDao.deleteAllServers()
                SKPreferences.remove(SKConstantesUtils.AUTHENTICATED)
                serverDao.addServer(SKAppConfigUtil.FixedServer!!)
            }
        }
        when {
            SKPreferences.contains(SKConstantesUtils.AUTHENTICATED) -> {
                var resting: Int? = null
                if (SKPreferences.contains(SKConstantesUtils.TASTING)) {
                    resting = SKLoginService.getTastingDays(SKPreferences.getLong(SKConstantesUtils.TASTING))
                }
                if (resting == null || resting > 0) {
                    val i = Intent(this, SKAppConfigUtil.mainActivity!!::class.java)
                    startActivity(i)
                    //todo fazer uma mensagem para falar q degustacao
                    finish()
                } else {
                    //todo fazer uma mensagem para falar q degustacao acabou
                    SKLoginService.doLogout()
                }
            }
            serverDao.getServerCount() > 0 -> {
                val i = Intent(this, SKAppConfigUtil.loginActivity!!::class.java)
                startActivity(i)
                finish()
            }
            else -> {
                when {
                    SKAppConfigUtil.FixedServer == null -> {
                        val i = Intent(this, SKAppConfigUtil.serverActivity!!::class.java)
                        startActivity(i)
                    }
                    else -> {
                        serverDao.addServer(SKAppConfigUtil.FixedServer!!)
                        val i = Intent(this, SKAppConfigUtil.loginActivity!!::class.java)
                        startActivity(i)

                    }
                }
                finish()

            }
        }
    }


    abstract fun errorMenssage()
    abstract fun errorNetworkMenssage()
    open fun tryAgain() {
        initData()
    }
}