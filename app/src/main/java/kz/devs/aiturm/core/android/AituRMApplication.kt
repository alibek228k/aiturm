package kz.devs.aiturm.core.android

import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kz.devs.aiturm.MainActivity
import kz.devs.aiturm.di.aituRmModules
import kz.devs.aiturm.presentaiton.SessionManager
import kz.garage.locale.LocaleManager
import kz.garage.locale.base.LocaleManagerBaseApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import java.util.Locale

class AituRMApplication : LocaleManagerBaseApplication() {

    private var mAuth: FirebaseAuth? = null

    private var isInitialized = false
    private var manager: SessionManager? = null

    override fun initializeLocaleManager() {
        LocaleManager.initialize(
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
        startKoin {
            androidLogger()
            androidContext(this@AituRMApplication)
            modules(aituRmModules)
        }
        manager = SessionManager(applicationContext)
        if (manager?.isInitialized() == true) {
            mAuth = FirebaseAuth.getInstance()
            val user = mAuth?.currentUser
            if (user != null) {
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
}