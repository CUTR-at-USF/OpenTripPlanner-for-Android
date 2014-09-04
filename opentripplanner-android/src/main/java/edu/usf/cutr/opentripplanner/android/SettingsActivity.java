/*
 * Copyright 2011 Marcy Gordon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.usf.cutr.opentripplanner.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import edu.usf.cutr.opentripplanner.android.listeners.ServerCheckerCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;
import edu.usf.cutr.opentripplanner.android.tasks.ServerChecker;
import edu.usf.cutr.opentripplanner.android.util.ConversionUtils;

;

/*
 * Modified by Khoa Tran
 */
public class SettingsActivity extends PreferenceActivity implements ServerCheckerCompleteListener {

    private ListPreference mapTileProvider;

    private CheckBoxPreference autoDetectServer;

    private EditTextPreference customServerURL;

    private Preference providerFeedbackButton;

    private CheckBoxPreference wheelAccessible;

    private Preference serverRefreshButton;

    private Preference aboutButton;

    private CheckBoxPreference selectedCustomServer;

    private CheckBoxPreference liveUpdates;

    private PreferenceCategory liveUpdatesCategory;

    private PreferenceScreen preferenceScreen;

    private ListPreference geocoderProvider;

    private EditTextPreference maxWalkingDistance;

    private Intent returnIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        returnIntent = new Intent();

        if (savedInstanceState != null) {
            if ((returnIntent = savedInstanceState.getParcelable(OTPApp.BUNDLE_KEY_SETTINGS_INTENT))
                    != null) {
                setResult(RESULT_OK, returnIntent);
            }
        }

        addPreferencesFromResource(R.xml.preferences);

        mapTileProvider = (ListPreference) findPreference(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE);
        geocoderProvider = (ListPreference) findPreference(OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER);
        autoDetectServer = (CheckBoxPreference) findPreference(
                OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER);
        customServerURL = (EditTextPreference) findPreference(
                OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL);
        selectedCustomServer = (CheckBoxPreference) findPreference(
                OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER);
        liveUpdates = (CheckBoxPreference) findPreference(
                OTPApp.PREFERENCE_KEY_LIVE_UPDATES);
        liveUpdatesCategory = (PreferenceCategory) findPreference(
                OTPApp.PREFERENCE_KEY_LIVE_UPDATES_CATEGORY);
        preferenceScreen = (PreferenceScreen) findPreference(
                OTPApp.PREFERENCE_KEY_PREFERENCE_SCREEN);
        maxWalkingDistance = (EditTextPreference) findPreference(
                OTPApp.PREFERENCE_KEY_MAX_WALKING_DISTANCE);
        wheelAccessible = (CheckBoxPreference) findPreference(
                OTPApp.PREFERENCE_KEY_WHEEL_ACCESSIBLE);
        aboutButton = findPreference(
                OTPApp.PREFERENCE_KEY_ABOUT);

        String[] entriesArray = getResources().getStringArray(R.array.map_tiles_servers_names);
        ArrayList<String> entries = new ArrayList<String>(Arrays.asList(entriesArray));
        entries.add(OTPApp.MAP_TILE_GOOGLE_NORMAL);
        entries.add(OTPApp.MAP_TILE_GOOGLE_SATELLITE);
        entries.add(OTPApp.MAP_TILE_GOOGLE_HYBRID);
        entries.add(OTPApp.MAP_TILE_GOOGLE_TERRAIN);
        mapTileProvider.setEntries(entries.toArray(new CharSequence[entries.size()]));

        String[] entriesValuesArray = getResources().getStringArray(R.array.map_tiles_servers_urls);
        ArrayList<String> entriesValues = new ArrayList<String>(Arrays.asList(entriesValuesArray));
        entriesValues.add(OTPApp.MAP_TILE_GOOGLE_NORMAL);
        entriesValues.add(OTPApp.MAP_TILE_GOOGLE_SATELLITE);
        entriesValues.add(OTPApp.MAP_TILE_GOOGLE_HYBRID);
        entriesValues.add(OTPApp.MAP_TILE_GOOGLE_TERRAIN);
        mapTileProvider
                .setEntryValues(entriesValues.toArray(new CharSequence[entriesValues.size()]));
        if (mapTileProvider.getValue() == null) {
            mapTileProvider.setValue(ConversionUtils.getOverlayString(this));
        }

        CharSequence geocoders[] = {getResources().getString(R.string.geocoder_nominatim),
                getResources().getString(R.string.geocoder_google_places)};
        geocoderProvider.setEntries(geocoders);
        geocoderProvider.setEntryValues(geocoders);
        if (geocoderProvider.getValue() == null) {
            geocoderProvider.setValue(getResources().getString(R.string.geocoder_nominatim));
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if (prefs.getString(OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER,
                getResources().getString(R.string.geocoder_nominatim))
                .equals(getResources().getString(R.string.geocoder_nominatim))) {
            geocoderProvider.setSummary(
                    getResources().getString(R.string.settings_menu_geocoder_preference_provider_nominatim));
        } else {
            geocoderProvider.setSummary(
                    getResources().getString(R.string.settings_menu_geocoder_preference_provider_google_places));
        }

        geocoderProvider.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;

                if (value.equals(getResources().getString(R.string.geocoder_nominatim))) {
                    geocoderProvider.setSummary(getResources()
                            .getString(R.string.settings_menu_geocoder_preference_provider_nominatim));
                } else {
                    geocoderProvider.setSummary(getResources()
                            .getString(R.string.settings_menu_geocoder_preference_provider_google_places));
                }

                return true;
            }

        });

        String actualMapTileProvider = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE,
                getResources().getString(R.string.map_tiles_default_server_low_res));

        if (actualMapTileProvider.equals(getResources().getString(R.string.tiles_mapnik))) {
            mapTileProvider.setSummary(getResources().getString(R.string.mapnik));
        } else if (actualMapTileProvider.equals(getResources().getString(R.string.tiles_maquest))) {
            mapTileProvider.setSummary(getResources().getString(R.string.maquest));
        } else if (actualMapTileProvider
                .equals(getResources().getString(R.string.tiles_cyclemap))) {
            mapTileProvider.setSummary(getResources().getString(R.string.cyclemap));
        } else if (actualMapTileProvider
                .equals(getResources().getString(R.string.tiles_lyrk))) {
            mapTileProvider.setSummary(getResources().getString(R.string.lyrk));
        } else if (actualMapTileProvider
                .equals(getResources().getString(R.string.tiles_mapbox))) {
            mapTileProvider.setSummary(getResources().getString(R.string.mapbox));
        } else if (actualMapTileProvider
                .equals(getResources().getString(R.string.tiles_mapbox_retina))) {
            mapTileProvider.setSummary(getResources().getString(R.string.mapboxretina));
        } else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_NORMAL)) {
            mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
        } else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_HYBRID)) {
            mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_HYBRID);
        } else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_SATELLITE)) {
            mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_SATELLITE);
        } else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_TERRAIN)) {
            mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_TERRAIN);
        }

        mapTileProvider.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;

                returnIntent.putExtra(OTPApp.CHANGED_MAP_TILE_PROVIDER_RETURN_KEY, true);
                setResult(RESULT_OK, returnIntent);

                if (value.equals(getResources().getString(R.string.tiles_mapnik))) {
                    mapTileProvider.setSummary(getResources().getString(R.string.mapnik));
                } else if (value.equals(getResources().getString(R.string.tiles_maquest))) {
                    mapTileProvider.setSummary(getResources().getString(R.string.maquest));
                } else if (value.equals(getResources().getString(R.string.tiles_cyclemap))) {
                    mapTileProvider.setSummary(getResources().getString(R.string.cyclemap));
                } else if (value.equals(getResources().getString(R.string.tiles_lyrk))) {
                    mapTileProvider.setSummary(getResources().getString(R.string.lyrk));
                } else if (value.equals(getResources().getString(R.string.tiles_mapbox))) {
                    mapTileProvider.setSummary(getResources().getString(R.string.mapbox));
                } else if (value.equals(getResources().getString(R.string.tiles_mapbox_retina))) {
                    mapTileProvider.setSummary(getResources().getString(R.string.mapboxretina));
                } else if (value.equals(OTPApp.MAP_TILE_GOOGLE_NORMAL)) {
                    mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
                } else if (value.equals(OTPApp.MAP_TILE_GOOGLE_HYBRID)) {
                    mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_HYBRID);
                } else if (value.equals(OTPApp.MAP_TILE_GOOGLE_SATELLITE)) {
                    mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_SATELLITE);
                } else if (value.equals(OTPApp.MAP_TILE_GOOGLE_TERRAIN)) {
                    mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_TERRAIN);
                }

                return true;
            }

        });

        maxWalkingDistance.setSummary(maxWalkingDistance.getText() + " " + getResources()
                .getString(R.string.settings_menu_maximum_walk_description));

        maxWalkingDistance.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;

                maxWalkingDistance.setSummary(
                        value + " " + getResources().getString(R.string.settings_menu_maximum_walk_description));

                returnIntent.putExtra(OTPApp.CHANGED_PARAMETERS_MUST_REQUEST_TRIP_RETURN_KEY, true);
                setResult(RESULT_OK, returnIntent);

                return true;
            }

        });

        wheelAccessible.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                returnIntent.putExtra(OTPApp.CHANGED_PARAMETERS_MUST_REQUEST_TRIP_RETURN_KEY, true);
                setResult(RESULT_OK, returnIntent);

                return true;
            }

        });

        if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false)) {
            customServerURL
                    .setSummary(getResources().getString(R.string.settings_menu_custom_server_url_description));
        } else {
            selectedCustomServer.setEnabled(false);
            customServerURL.setSummary(getResources().getString(R.string.settings_menu_custom_server_url_description_error_url));
        }

        if (selectedCustomServer.isEnabled() && selectedCustomServer.isChecked()) {
            autoDetectServer.setEnabled(false);
        }

        selectedCustomServer.setDependency(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL);

        customServerURL.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;

                if (URLUtil.isValidUrl(value)) {
                    WeakReference<Activity> weakContext = new WeakReference<Activity>(
                            SettingsActivity.this);
                    ServerChecker serverChecker = new ServerChecker(weakContext,
                            SettingsActivity.this.getApplicationContext(), SettingsActivity.this,
                            true, true, false);
                    serverChecker.execute(
                            new Server(value, SettingsActivity.this.getApplicationContext()));
                    return true;
                }

                Toast.makeText(SettingsActivity.this.getApplicationContext(),
                        SettingsActivity.this.getApplicationContext().getResources()
                                .getString(R.string.settings_menu_custom_server_url_description_error_url), Toast.LENGTH_SHORT
                )
                        .show();

                customServerURL
                        .setSummary(getResources().getString(R.string.settings_menu_custom_server_url_description_error_url));

                setResult(RESULT_OK, returnIntent);
                return false;
            }
        });

        selectedCustomServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean value = (Boolean) newValue;
                if (value) {
                    autoDetectServer.setChecked(false);
                    autoDetectServer.setEnabled(false);
                } else {
                    autoDetectServer.setEnabled(true);
                }

                Log.d(OTPApp.TAG, "Custom server Button clicked");

                returnIntent.putExtra(OTPApp.CHANGED_SELECTED_CUSTOM_SERVER_RETURN_KEY, true);
                setResult(RESULT_OK, returnIntent);

                return true;
            }
        });

        autoDetectServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean value = (Boolean) newValue;
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());

                if (value) {
                    selectedCustomServer.setEnabled(false);
                } else {
                    if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false)) {
                        selectedCustomServer.setEnabled(true);
                    }
                }

                return true;
            }
        });

        if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_REALTIME_AVAILABLE, false)) {
            liveUpdates.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean value = (Boolean) newValue;
                    if (!value) {
                        returnIntent.putExtra(OTPApp.LIVE_UPDATES_DISABLED_RETURN_KEY, true);
                        setResult(RESULT_OK, returnIntent);
                    }
                    return true;
                }
            });
        } else {
            liveUpdatesCategory.removePreference(liveUpdates);
            preferenceScreen.removePreference(liveUpdatesCategory);
        }

        aboutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.v(OTPApp.TAG, "About button clicked");
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
        });

        providerFeedbackButton = (Preference) findPreference(
                OTPApp.PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK);
        providerFeedbackButton
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        Log.d(OTPApp.TAG, "Provider Feedback Button clicked");
                        String recipient = getString(R.string.feedback_email_android_developer);

                        String uriText = "mailto:" + recipient;

                        String subject = "";
                        subject += getResources().getString(R.string.menu_button_feedback_subject);
                        Date d = Calendar.getInstance().getTime();
                        subject += "[" + d.toString() + "]";
                        uriText += "?subject=" + subject;

                        Uri uri = Uri.parse(uriText);

                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                        sendIntent.setData(uri);
                        startActivity(Intent.createChooser(sendIntent,
                                getResources().getString(R.string.menu_button_feedback_send_email)));

                        return true;
                    }
                });

        ServersDataSource datasource = ServersDataSource.getInstance(this);
        datasource.open();
        Long mostRecentDate = datasource.getMostRecentDate();

        serverRefreshButton = (Preference) findPreference(
                OTPApp.PREFERENCE_KEY_REFRESH_SERVER_LIST);

        if (mostRecentDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(mostRecentDate);
            serverRefreshButton.setSummary(
                    getResources().getString(R.string.settings_menu_server_list_download_date_description)
                            + ConversionUtils.getTimeWithContext(this.getApplicationContext(),
                            cal.getTimeZone().getOffset(cal.getTimeInMillis()),
                            cal.getTimeInMillis(), true)
            );
        } else {
            serverRefreshButton.setSummary(
                    getResources().getString(R.string.settings_menu_server_list_download_date_unknown));
        }

        serverRefreshButton
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        Log.d(OTPApp.TAG, "Server Refresh Button clicked");

                        returnIntent.putExtra(OTPApp.REFRESH_SERVER_RETURN_KEY, true);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                        return true;
                    }
                });

        datasource.close();
    }


    @Override
    public void onServerCheckerComplete(String result, boolean isCustomServer,
                                        boolean isAutodetected, boolean isWorking) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        if (isWorking) {
            autoDetectServer.setChecked(false);
            autoDetectServer.setEnabled(false);
            selectedCustomServer.setEnabled(true);
            selectedCustomServer.setChecked(true);
            returnIntent.putExtra(OTPApp.CHANGED_SELECTED_CUSTOM_SERVER_RETURN_KEY, true);
            setResult(RESULT_OK, returnIntent);
            customServerURL
                    .setSummary(getResources().getString(R.string.settings_menu_custom_server_url_description));
            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, true);
        } else {
            autoDetectServer.setEnabled(true);
            selectedCustomServer.setChecked(false);
            selectedCustomServer.setEnabled(false);
            customServerURL.setSummary(getResources().getString(R.string.settings_menu_custom_server_url_description_error_unreachable));
            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false);
            prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false);
        }

        prefsEditor.commit();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (returnIntent != null) {
            outState.putParcelable(OTPApp.BUNDLE_KEY_SETTINGS_INTENT, returnIntent);
        }
    }


}
