package com.luckyxmobile.correction.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import com.luckyxmobile.correction.R;
import com.luckyxmobile.correction.util.ConstantsUtil;
import com.luckyxmobile.correction.util.SDCardUtil;

import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferences preferences;
        private SharedPreferences.Editor editor;

        private PreferenceScreen setTagPre;
        private MultiSelectListPreference printPagePre;
        private MultiSelectListPreference showSmearPre;
        private MultiSelectListPreference viewSmearByPre;
        private CheckBoxPreference fullScreenViewPagePre;
        private CheckBoxPreference showTagViewPagePre;
        private CheckBoxPreference printSmearPre;
        private PreferenceScreen versionPre;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_setting, rootKey);

            setTagPre = findPreference("set_tag_pre");
            printPagePre = findPreference("print_paper_pre");
            showSmearPre = findPreference("show_smear_pre");
            viewSmearByPre = findPreference("view_smear_by");
            fullScreenViewPagePre =  findPreference("view_pager_full_screen");
            showTagViewPagePre =  findPreference("view_pager_show_tag");
            printSmearPre = findPreference("print_page_show_smear");
            versionPre =  findPreference("version_pre");

            preferences = getActivity().getSharedPreferences(ConstantsUtil.TABLE_SHARED_CORRECTION,MODE_PRIVATE);

            setPaintPage();

            setShowSmear();

            setViewSmearBy();

            printSmearPre.setChecked(preferences.getBoolean(ConstantsUtil.TABLE_PRINT_SMEAR_CONTENT,true));

            showTagViewPagePre.setChecked(preferences.getBoolean(ConstantsUtil.TABLE_SHOW_TAG,true));

            fullScreenViewPagePre.setChecked(preferences.getBoolean(ConstantsUtil.TABLE_FULL_SCREEN,false));

            versionPre.setSummary("\t"+ SDCardUtil.packageName(getContext()));

        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {

            editor = preferences.edit();

            if (preference==setTagPre){
                startActivity(new Intent(getActivity(),SetTagActivity.class));
            }else if (preference==showTagViewPagePre){
                editor.putBoolean(ConstantsUtil.TABLE_SHOW_TAG,showTagViewPagePre.isChecked());
                editor.apply();
            }else if (preference==fullScreenViewPagePre){
                editor.putBoolean(ConstantsUtil.TABLE_FULL_SCREEN,fullScreenViewPagePre.isChecked());
                editor.apply();
            }else if (preference==printSmearPre){
                editor.putBoolean(ConstantsUtil.TABLE_PRINT_SMEAR_CONTENT,printSmearPre.isChecked());
                editor.apply();
            }

            return super.onPreferenceTreeClick(preference);
        }

        private void setViewSmearBy() {

            int smearBy = preferences.getInt(ConstantsUtil.TABLE_VIEW_SMEAR_BY,2);

            if (smearBy==2){
                viewSmearByPre.setSummary(getString(R.string.click_button_by)+","+getString(R.string.click_smear_by));
            }else if (smearBy == 1){
                viewSmearByPre.setSummary(getString(R.string.click_smear_by));
            }else if (smearBy == 0){
                viewSmearByPre.setSummary(getString(R.string.click_button_by));
            }

            viewSmearByPre.setOnPreferenceChangeListener((preference, newValue) -> {

                String selectViewSmearBy = newValue.toString().replace("[","");
                selectViewSmearBy = selectViewSmearBy.replace("]","");

                if (TextUtils.isEmpty(selectViewSmearBy)){
                    Toasty.error(getContext(), R.string.select_at_least_one,Toasty.LENGTH_SHORT,true).show();
                    return false;
                }else{
                    preference.setSummary(selectViewSmearBy);
                    String[] s = selectViewSmearBy.split(",");
                    if (s.length == 2){
                        editor.putInt(ConstantsUtil.TABLE_VIEW_SMEAR_BY,2);
                    }else{
                        if (s[0].equals(getString(R.string.click_smear_by))){
                            editor.putInt(ConstantsUtil.TABLE_VIEW_SMEAR_BY,1);
                        }else{
                            editor.putInt(ConstantsUtil.TABLE_VIEW_SMEAR_BY,0);
                        }
                    }
                    editor.apply();
                    return true;
                }
            });
        }

        private void setShowSmear() {

            showSmearPre.setSummary(preferences.getString(ConstantsUtil.TABLE_SHOW_SMEAR,getString(R.string.do_not_show)));

            showSmearPre.setOnPreferenceChangeListener((preference, newValue) -> {
                String selectPrint = newValue.toString().replace("[","");
                selectPrint = selectPrint.replace("]","");

                if (TextUtils.isEmpty(selectPrint)){
                    preference.setSummary(getString(R.string.do_not_show));
                }else{
                    preference.setSummary(selectPrint);
                }

                editor.putString(ConstantsUtil.TABLE_SHOW_SMEAR,preference.getSummary().toString());
                editor.apply();

                return true;

            });
        }

        private void setPaintPage() {

            printPagePre.setSummary(preferences.getString(ConstantsUtil.TABLE_PRINT_PAGE,getString(R.string.stem)));

            printPagePre.setOnPreferenceChangeListener((preference, newValue) -> {
                String selectPrint = newValue.toString().replace("[","");
                selectPrint = selectPrint.replace("]","");

                if (TextUtils.isEmpty(selectPrint)){
                    Toasty.error(getContext(), R.string.select_at_least_one,Toasty.LENGTH_SHORT,true).show();
                    return false;
                }else{
                    preference.setSummary(selectPrint);
                    editor.putString(ConstantsUtil.TABLE_PRINT_PAGE,selectPrint);
                    editor.apply();
                    return true;
                }

            });

        }


    }

}