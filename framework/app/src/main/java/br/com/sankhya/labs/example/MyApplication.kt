package br.com.sankhya.labs.example

import br.com.sankhya.labs.framework_core.controller.SKAppCompatActivity
import br.com.sankhya.labs.framework_core.SKApplication

class MyApplication:SKApplication(){
    override fun loginTheme(): Int? {
        return null
    }

    override fun isJIVA(): Boolean {
        return false
    }

    override fun productCodePush(): String? {
        return null
    }

    override fun clearPreferencesInLogout(): Boolean {
      return  false
    }

    override fun productCode(): String? {
        return null
    }

    override fun mainTheme(): Int? {
       return null
    }

    override fun loginActivity(): SKAppCompatActivity {
        return MainActivity()
    }

    override fun mainActivity(): SKAppCompatActivity {
        return MainActivity()
    }

    override fun serverActivity(): SKAppCompatActivity {
        return MainActivity()
    }

    override fun splashScreenTheme(): Int? {
        return null
    }







}