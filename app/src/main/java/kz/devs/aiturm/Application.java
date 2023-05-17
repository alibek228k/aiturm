package kz.devs.aiturm;

import com.google.firebase.FirebaseApp;

public class Application extends android.app.Application {
    private boolean isInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!isInitialized) {
            FirebaseApp.initializeApp(getApplicationContext());
            isInitialized = true;
        }
    }
}
