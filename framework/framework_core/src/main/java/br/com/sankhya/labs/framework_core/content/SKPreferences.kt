package br.com.sankhya.labs.framework_core.content

import android.content.Context
import android.content.SharedPreferences
import br.com.sankhya.labs.framework_core.SKApplication
import br.com.sankhya.labs.framework_core.utils.SKAppConfigUtil
import com.google.gson.Gson
import java.lang.reflect.Type
import java.util.HashSet


object SKPreferences {

        private fun get(): SharedPreferences = SKApplication.context.getSharedPreferences("SK_" + SKAppConfigUtil.ProductCode + "_PREFERENCES", Context.MODE_PRIVATE)

        fun clear() = get().edit().clear().apply()

        fun remove(key: String) = get().edit().remove(key).apply()

        fun contains(key: String): Boolean = get().contains(key)

        fun set(key: String, value: String) = get().edit().putString(key, value).apply()

        fun getString(key: String, if_not_exists: String?): String? = get().getString(key, if_not_exists)

        fun getString(key: String): String? = getString(key, null)

        fun set(key: String, value: Boolean) = get().edit().putBoolean(key, value).apply()

        fun getBoolean(key: String, if_not_exists: Boolean): Boolean = get().getBoolean(key, if_not_exists)

        fun getBoolean(key: String): Boolean = getBoolean(key, false)

        fun set(key: String, value: Float) = get().edit().putFloat(key, value).apply()

        private fun getFloat(key: String, if_not_exists: Float): Float = get().getFloat(key, if_not_exists)

        fun getFloat(key: String): Float = getFloat(key, 0f)

        fun set(key: String, value: Int) = get().edit().putInt(key, value).apply()

        private fun getInt(key: String, if_not_exists: Int): Int = get().getInt(key, if_not_exists)

        fun getInt(key: String): Int = getInt(key, 0)

        fun set(key: String, value: Long) = get().edit().putLong(key, value).apply()

        private fun getLong(key: String, if_not_exists: Long): Long = get().getLong(key, if_not_exists)

        fun getLong(key: String): Long = getLong(key, 0)

        fun set(key: String, value: Set<String>) {
            remove(key)
            get().edit().putStringSet(key, value).apply()
        }

        private fun getStringSet(key: String, if_not_exists: Set<String>): Set<String>? = get().getStringSet(key, if_not_exists)

        fun getStringSet(key: String): Set<String>? = getStringSet(key, HashSet())

        fun set(key: String, `object`: Any?, acceptNull: Boolean = false) = when {
            acceptNull || `object` != null -> set(key, Gson().toJson(`object`))
            else -> remove(key)
        }
        fun get(c: Class<*>, key: String): Any? = if (contains(key)) Gson().fromJson(getString(key), c) else null

        fun get(t: Type, key: String): Any? = if (contains(key)) Gson().fromJson<Any>(getString(key), t) else null
}