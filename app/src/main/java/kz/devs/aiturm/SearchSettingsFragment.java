package kz.devs.aiturm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.shroomies.R;
import com.nfx.android.rangebarpreference.RangeBarPreferenceCompat;

public class SearchSettingsFragment extends PreferenceFragmentCompat {
    SwitchPreferenceCompat priceFilterSwitchPreference, preferencesSwitch;
    RangeBarPreferenceCompat rangeBarPreferenceCompat;
    MultiSelectListPreference multiSelectListPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rangeBarPreferenceCompat = findPreference("range_preference");
        priceFilterSwitchPreference = findPreference("filter_price");
        preferencesSwitch = findPreference("filter_preferences");
        multiSelectListPreference = findPreference("properties");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        boolean filterPrice = prefs.getBoolean("filter_price", false);
        rangeBarPreferenceCompat.setEnabled(filterPrice);

        boolean preferenceEnable = prefs.getBoolean("filter_preferences", false);
        multiSelectListPreference.setEnabled(preferenceEnable);

        preferencesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            multiSelectListPreference.setEnabled((boolean) newValue);
            return true;
        });

        priceFilterSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            rangeBarPreferenceCompat.setEnabled((boolean) newValue);
            return true;
        });

    }
}
