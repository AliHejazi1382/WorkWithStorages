package com.example.workwithstorages;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

public class MyFragmentSettings extends PreferenceFragmentCompat {
    private SharedPreferences preferences;
    private Context context;
    private PreferenceScreen screen;
    private SwitchPreferenceCompat encryptedNotes;
    private PreferenceCategory security;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        initFields();
        loadFields();
        loadFields();
        makeFields();
        setPreferenceScreen(screen);
    }

    private void initFields() {
        this.context = getPreferenceManager().getContext();
        this.screen = getPreferenceManager().createPreferenceScreen(this.context);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.encryptedNotes = new SwitchPreferenceCompat(this.context);
        this.encryptedNotes.setKey("encryptedNotes");
        this.security = new PreferenceCategory(this.context);
        this.security.setKey("security");
    }

    private void loadFields() {
        if (preferences.contains("encryptedNotes")){
            boolean isChecked = preferences.getBoolean("encryptedNotes", false);
            if (isChecked){
                encryptedNotes.setIcon(R.drawable.encrypted);
            } else {
                encryptedNotes.setIcon(R.drawable.no_encryption);
            }
        }
    }

    private void makeFields() {
        encryptedNotes.setSummaryOff("The notes will not be read and written encryptly.(Recommended)");
        encryptedNotes.setSummaryOn("The notes will be read and written encryptly\nThis may take up too much of your memory");
        encryptedNotes.setTitle("Encrypted Mode");
        encryptedNotes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isChecked = (boolean) newValue;
                if (isChecked){
                    encryptedNotes.setIcon(R.drawable.encrypted);
                } else {
                    encryptedNotes.setIcon(R.drawable.no_encryption);
                }
                return true;
            }
        });

        security.setTitle("Encrypted Data");
        screen.addPreference(security);
        security.addPreference(encryptedNotes);
    }
}
