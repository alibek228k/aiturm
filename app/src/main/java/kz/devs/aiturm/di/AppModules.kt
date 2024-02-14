package kz.devs.aiturm.di

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kz.devs.aiturm.R
import kz.devs.aiturm.presentaiton.SessionManager
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val firebaseModule = module {
    factory {
        FirebaseAuth.getInstance()
    }

    factory<GoogleSignInOptions> {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(androidApplication().getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    factory<GoogleSignInClient> {
        GoogleSignIn.getClient(androidApplication(), get<GoogleSignInOptions>())
    }

    factory {
        FirebaseDatabase.getInstance().reference
    }
}

val coreModule = module {
    factory {
        SessionManager(androidApplication())
    }
}

val aituRmModules = listOf(
    loginModule,
    firebaseModule,
    coreModule,
)