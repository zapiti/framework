package br.com.sankhya.labs.framework_core.service

import br.com.sankhya.labs.framework_core.model.SKServer

object SKServerService {
    fun isSystemCompatible(server: SKServer): Boolean {
        return true
    }
}