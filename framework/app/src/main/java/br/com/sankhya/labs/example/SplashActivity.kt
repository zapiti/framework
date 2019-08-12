package br.com.sankhya.labs.example

import android.os.Bundle
import br.com.sankhya.labs.framework_core.controller.SKSplashScream

class SplashActivity : SKSplashScream() {

    override fun errorNetworkMenssage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun errorMenssage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}
