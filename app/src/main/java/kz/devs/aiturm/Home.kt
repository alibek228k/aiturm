package kz.devs.aiturm

import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kz.devs.aiturm.presentaiton.SessionManager
import kz.devs.aiturm.utils.ContextUtils
import kz.garage.locale.LocaleManager.initialize
import kz.garage.locale.base.LocaleManagerBaseApplication
import java.util.*

class Home : LocaleManagerBaseApplication() {
    var mAuth: FirebaseAuth? = null

    private var isInitialized = false
    private var manager: SessionManager? = null

    override fun initializeLocaleManager() {
        initialize(
            context = this,
            supportedLocales = listOf(
                Locale.ENGLISH,
                Locale("ru"),
                Locale("kk")
            )
        )
    }

    override fun onCreate() {
        super.onCreate()
        manager = SessionManager(applicationContext)
        if (manager?.isInitialized() == true) {
            mAuth = FirebaseAuth.getInstance()
            val user = mAuth?.currentUser
            if (user != null) {
//                selectedLanguage = manager?.getLanguageDate()
//                setLanguage()
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } else {
            manager?.saveDataFirstTime()
        }
        if (!isInitialized) {
            FirebaseApp.initializeApp(applicationContext)
            isInitialized = true
        }
    }

//    private fun setLanguage() {
//        val contextUtils = ContextUtils.newInstance(this)
//        when (selectedLanguage) {
//            0L -> {
//                val resources = contextUtils.updateLocale("en").resources
//                val configuration = resources.configuration
//                val displayMetrics = resources.displayMetrics
//                windowManager.defaultDisplay.getMetrics(displayMetrics)
//                this.applicationContext.createConfigurationContext(configuration);
//            }
//            1L -> {
//                val resources = contextUtils.updateLocale("ru").resources
//                val configuration = resources.configuration
//                val displayMetrics = resources.displayMetrics
//                windowManager.defaultDisplay.getMetrics(displayMetrics)
//                this.applicationContext.createConfigurationContext(configuration);
//            }
//            2L -> {
//                val resources = contextUtils.updateLocale("kk").resources
//                val configuration = resources.configuration
//                val displayMetrics = resources.displayMetrics
//                windowManager.defaultDisplay.getMetrics(displayMetrics)
//                this.applicationContext.createConfigurationContext(configuration);
//            }
//        }
//    }
}