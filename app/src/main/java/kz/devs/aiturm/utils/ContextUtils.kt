package kz.devs.aiturm.utils

import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import kz.devs.aiturm.presentaiton.SessionManager
import java.util.*

class ContextUtils(
    private val context: Context
) : ContextWrapper(context) {

    companion object {
        fun newInstance(context: Context): ContextUtils = ContextUtils(context)
    }

    fun updateLanguage(selectedLanguageId: Long?): Context {

        return when (selectedLanguageId) {
            0L -> {
                val locale = Locale("en")
                Locale.setDefault(locale)
                val configuration = context.resources.configuration
                configuration.setLocale(locale)
                configuration.setLayoutDirection(locale)
                this.context.createConfigurationContext(configuration)
            }
            1L -> {
                val locale = Locale("ru")
                Locale.setDefault(locale)
                val configuration = context.resources.configuration
                configuration.setLocale(locale)
                configuration.setLayoutDirection(locale)
                this.context.createConfigurationContext(configuration)
                }
            2L -> {
                val locale = Locale("kk")
                Locale.setDefault(locale)
                val configuration = context.resources.configuration
                configuration.setLocale(locale)
                configuration.setLayoutDirection(locale)
                this.context.createConfigurationContext(configuration)
            }
            else -> this
        }
    }
}