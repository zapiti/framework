package br.com.sankhya.labs.framework_core.utils


import android.content.Context
import android.net.ConnectivityManager
import br.com.sankhya.labs.framework_core.SKApplication

object SKUtils {

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            SKApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    fun getApplicationName(context: Context = SKApplication.context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return(if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else
            context.getString(stringId)).replace("\\s+".toRegex(), "")
    }


}