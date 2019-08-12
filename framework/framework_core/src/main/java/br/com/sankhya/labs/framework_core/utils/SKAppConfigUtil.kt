package br.com.sankhya.labs.framework_core.utils

import br.com.sankhya.labs.framework_core.R
import br.com.sankhya.labs.framework_core.controller.SKAppCompatActivity
import br.com.sankhya.labs.framework_core.model.SKServer


internal object SKAppConfigUtil {


    var loginTheme: Int = 0
    var clearPreferencesInLogout: Boolean =  true
    var splashscreenTheme:Int = 0
    var mainTheme:Int? = null
    var mainActivity: SKAppCompatActivity? = null
    var loginActivity: SKAppCompatActivity? = null
    var serverActivity: SKAppCompatActivity? = null
    var activity: SKAppCompatActivity? = null


    var ProductCode: String? = null
    var ProductCodePush: String? = null
    var AppVersion : String? = null
    var FixedServer: SKServer? = null

    var isJIVA:Boolean = false
    var isSANKHYA:Boolean = false
}