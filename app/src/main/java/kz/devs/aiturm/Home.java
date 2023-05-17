package kz.devs.aiturm;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import kz.devs.aiturm.presentaiton.SessionManager;


public class Home extends Application {
    FirebaseAuth mAuth;

    private boolean isInitialized = false;
    private SessionManager manager;
    private static final Pattern pattern = Pattern.compile(Config.USERNAME_PATTERN);

    @Override
    public void onCreate() {
        super.onCreate();
        manager = new SessionManager(getApplicationContext());
        if (manager.isInitialized()){
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }else{
            manager.saveDataFirstTime();
        }

        if (!isInitialized){
            FirebaseApp.initializeApp(getApplicationContext());
            isInitialized = true;
        }
    }
}
