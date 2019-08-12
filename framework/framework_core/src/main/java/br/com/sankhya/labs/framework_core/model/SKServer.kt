package br.com.sankhya.labs.framework_core.model

class SKServer(var protocolo: String = "https",
               var host: String = "",
               var nome: String = "") {

    var escolhido: Long? = null

    fun getDomain(): String = "https://skwsoft01.sankhya.com.br"

//todo alterrar
//        if (host == "") {
//        ""
//    } else {
//        "$protocolo://${(if (host.contains("://")) host.split("://")[1] else host).split("/")[0]}".toLowerCase()
//    }

}