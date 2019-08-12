package br.com.sankhya.labs.framework_core.extension

import android.util.Base64

fun String.decodeBase64(): String = String(Base64.decode(this, Base64.DEFAULT))

fun String.encodeBase64(): String = String(Base64.encode(this.toByteArray(), Base64.DEFAULT))
