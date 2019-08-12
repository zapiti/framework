package br.com.sankhya.labs.framework_core

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import br.com.sankhya.labs.framework_core.controller.SKAppCompatActivity
import br.com.sankhya.labs.framework_core.utils.SKAppConfigUtil
import br.com.sankhya.labs.framework_core.utils.SKConstantesUtils
import com.google.firebase.FirebaseApp

abstract class SKApplication : Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        //activitys
        SKAppConfigUtil.loginActivity = loginActivity()
        SKAppConfigUtil.mainActivity = mainActivity()
        SKAppConfigUtil.serverActivity = serverActivity()

        //configis
        SKAppConfigUtil.ProductCode = productCode()
        SKAppConfigUtil.clearPreferencesInLogout = clearPreferencesInLogout()
        SKAppConfigUtil.ProductCodePush = productCodePush()
        SKAppConfigUtil.AppVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

        if(!isJIVA()){
            SKAppConfigUtil.isSANKHYA = true
        }
        SKAppConfigUtil.isJIVA = isJIVA()

        setThemes()

    }

    private fun setThemes() {
        if (isJIVA()) {
            SKAppConfigUtil.splashscreenTheme = splashScreenTheme() ?: R.style.SplashThemeJiva
            SKAppConfigUtil.mainTheme = mainTheme() ?: R.style.AppThemeJiva
            SKAppConfigUtil.loginTheme = loginTheme() ?: R.style.LoginThemeJiva
        } else {
            SKAppConfigUtil.splashscreenTheme = splashScreenTheme() ?: R.style.SplashThemeSankhya
            SKAppConfigUtil.mainTheme = mainTheme() ?: R.style.AppThemeSankhya
            SKAppConfigUtil.loginTheme = loginTheme() ?: R.style.LoginThemeSankhya
        }
    }

    abstract fun loginActivity(): SKAppCompatActivity
    abstract fun mainActivity(): SKAppCompatActivity
    abstract fun serverActivity(): SKAppCompatActivity

    abstract fun isJIVA():Boolean


    abstract fun mainTheme(): Int?
    abstract fun splashScreenTheme(): Int?
    abstract fun loginTheme(): Int?


    abstract fun productCode(): String?
    abstract fun productCodePush(): String?
    abstract fun clearPreferencesInLogout():Boolean

}