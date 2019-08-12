package br.com.sankhya.labs.framework_core.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.sankhya.labs.framework_core.utils.SKAppConfigUtil

open class SKAppCompatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(SKAppConfigUtil.mainTheme != null){
            setTheme(SKAppConfigUtil.mainTheme!!)
        }
        SKAppConfigUtil.activity = this
    }
}