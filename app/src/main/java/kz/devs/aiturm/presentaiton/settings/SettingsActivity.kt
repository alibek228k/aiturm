package kz.devs.aiturm.presentaiton.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import kz.devs.aiturm.R
import kz.devs.aiturm.presentaiton.SessionManager
import kz.garage.locale.base.LocaleManagerBaseActivity
import java.util.Locale


class SettingsActivity : LocaleManagerBaseActivity() {

    companion object {
        const val KEY_SETTINGS = "SETTINGS_KEY"

        fun newInstance(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

    private var toolbar: MaterialToolbar? = null
    private var languagePickerTextView: TextView? = null
    private var languageSpinner: Spinner? = null
    private var doneButton: ShapeableImageView? = null

    private var manager: SessionManager? = null

    private var selectedLanguage: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        manager = SessionManager(this)

        toolbar = findViewById(R.id.toolbar)
        languagePickerTextView = findViewById(R.id.language_picker_text_view)
        languageSpinner = findViewById(R.id.language_spinner)
        doneButton = findViewById(R.id.done_button)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        setupLanguagePicker()
        setupDoneButton()
    }

    private fun setupLanguagePicker() {
        ArrayAdapter.createFromResource(
            this,
            R.array.spinner_language,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            languageSpinner?.adapter = adapter
            val language = manager?.getLanguageDate() ?: -1L
            if (language == -1L) {
                languageSpinner?.setSelection(0)
            } else {
                languageSpinner?.setSelection(language.toInt())
            }
        }

        languageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedLanguage = id
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    private fun setupDoneButton() {
        doneButton?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.change_settings_message))
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton(R.string.yes) { dialog, _ ->
                    dialog.dismiss()
                    manager?.saveLanguageDate(selectedLanguage)
                    val locale = when (selectedLanguage) {
                        0L -> "en"
                        1L -> "ru"
                        2L -> "kk"
                        else -> return@setPositiveButton
                    }
                    setLocale(Locale(locale))
                }.show()
        }
    }
}