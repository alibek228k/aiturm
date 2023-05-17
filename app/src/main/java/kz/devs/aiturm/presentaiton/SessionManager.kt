package kz.devs.aiturm.presentaiton

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kz.devs.aiturm.model.User

class SessionManager(
    private val context: Context
) {
    companion object {
        private const val PREFERENCES_USERS_KEY = "USERS"
        private const val PREFERENCES_APP_KEY = "APP"
    }

    private var preferences: SharedPreferences? = null
    private val gson: Gson = Gson()

    fun saveData(user: User): Boolean {
        preferences = context.getSharedPreferences(PREFERENCES_USERS_KEY, Context.MODE_PRIVATE) ?: return false
        preferences?.edit()?.apply {
            putString("user", gson.toJson(user))
            apply()
            return true
        }
        return false
    }

    fun getData(): User{
        preferences = context.getSharedPreferences(PREFERENCES_USERS_KEY, Context.MODE_PRIVATE)
        val userString = preferences?.getString("user", null)
        return gson.fromJson(userString, User::class.java)
    }

    fun removeUserData(): Boolean{
        preferences = context.getSharedPreferences(PREFERENCES_USERS_KEY, Context.MODE_PRIVATE)
        preferences?.edit()?.apply {
            clear()
            apply()
            return true
        }
        return false
    }

    fun saveDataFirstTime(){
        preferences = context.getSharedPreferences(PREFERENCES_APP_KEY, Context.MODE_PRIVATE) ?: return
        preferences?.edit()?.apply {
            putBoolean("isInitialized", true)
            apply()
        }
    }

    fun isInitialized(): Boolean {
        preferences =
            context.getSharedPreferences(PREFERENCES_APP_KEY, Context.MODE_PRIVATE) ?: return false
        return preferences?.getBoolean("isInitialized", false) ?: false
    }

}