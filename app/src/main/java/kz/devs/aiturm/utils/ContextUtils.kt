package kz.devs.aiturm.utils

import android.content.Context
import android.content.ContextWrapper
import java.util.*

class ContextUtils(
    private val context: Context
) : ContextWrapper(context) {

    companion object {
        fun newInstance(context: Context): ContextUtils = ContextUtils(context)
    }

    fun updateLocale(language: String): Context{
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}