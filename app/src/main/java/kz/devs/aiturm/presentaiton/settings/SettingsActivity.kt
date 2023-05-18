package kz.devs.aiturm.presentaiton.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.shroomies.R
import com.google.android.material.appbar.MaterialToolbar
import kz.devs.aiturm.presentaiton.utils.LocaleManager
import java.util.*


class SettingsActivity : AppCompatActivity() {

    companion object{
        fun newInstance(context: Context): Intent{
            return Intent(context, SettingsActivity::class.java)
        }
    }

    private var toolbar: MaterialToolbar?= null
    private var languagePickerTextView: TextView?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        toolbar = findViewById(R.id.toolbar)
        languagePickerTextView = findViewById(R.id.language_picker_text_view)
        setSupportActionBar(toolbar)

        languagePickerTextView?.setOnClickListener{
            registerForContextMenu(languagePickerTextView)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // you can set menu header with title icon etc
        menu.setHeaderTitle("Choose a language")
        // add menu items
        menu.add(0, v.getId(), 0, "Қазақша")
        menu.add(0, v.getId(), 0, "English")
        menu.add(0, v.getId(), 0, "Русский")
    }

    // menu item select listener
    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.title === "Қазақша") {
            val context = LocaleManager.setLocale(this, "kk")
        } else if (item.title === "English") {
            val context = LocaleManager.setLocale(this, "en")
        } else if (item.title === "Русский") {
            val context = LocaleManager.setLocale(this, "ru")
        }
        return true
    }
}