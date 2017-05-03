/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.PreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settings.fuelgauge.anomaly.AnomalyDialogFragment;
import com.android.settings.fuelgauge.anomaly.AnomalyPreference;

import java.util.List;

/**
 * Fragment to show a list of anomaly apps, where user could handle these anomalies
 */
public class PowerUsageAnomalyDetails extends DashboardFragment implements
        AnomalyDialogFragment.AnomalyDialogListener {

    public static final String TAG = "PowerAbnormalUsageDetail";
    @VisibleForTesting
    static final String EXTRA_ANOMALY_LIST = "anomaly_list";
    private static final int REQUEST_ANOMALY_ACTION = 0;
    private static final String KEY_PREF_ANOMALY_LIST = "app_abnormal_list";

    @VisibleForTesting
    List<Anomaly> mAnomalies;
    @VisibleForTesting
    PreferenceGroup mAbnormalListGroup;

    public static void startBatteryAbnormalPage(SettingsActivity caller,
            PreferenceFragment fragment, List<Anomaly> anomalies) {
        Bundle args = new Bundle();
        args.putParcelableList(EXTRA_ANOMALY_LIST, anomalies);

        caller.startPreferencePanelAsUser(fragment, PowerUsageAnomalyDetails.class.getName(), args,
                R.string.battery_abnormal_details_title, null,
                new UserHandle(UserHandle.myUserId()));
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAnomalies = getArguments().getParcelableArrayList(EXTRA_ANOMALY_LIST);
        mAbnormalListGroup = (PreferenceGroup) findPreference(KEY_PREF_ANOMALY_LIST);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshUi();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof AnomalyPreference) {
            AnomalyPreference anomalyPreference = (AnomalyPreference) preference;
            final Anomaly anomaly = anomalyPreference.getAnomaly();

            AnomalyDialogFragment dialogFragment = AnomalyDialogFragment.newInstance(anomaly);
            dialogFragment.setTargetFragment(this, REQUEST_ANOMALY_ACTION);
            dialogFragment.show(getFragmentManager(), TAG);

            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.power_abnormal_detail;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        return null;
    }

    @Override
    public int getMetricsCategory() {
        //TODO(b/37681923): add correct metrics category
        return 0;
    }

    void refreshUi() {
        //TODO(b/37681665): cache the preference so we don't need to create new one every time.
        mAbnormalListGroup.removeAll();
        for (int i = 0, size = mAnomalies.size(); i < size; i++) {
            final Anomaly anomaly = mAnomalies.get(i);
            Preference pref = new AnomalyPreference(getPrefContext(), anomaly);

            mAbnormalListGroup.addPreference(pref);
        }
    }

    @Override
    public void onAnomalyHandled(Anomaly anomaly) {
        mAnomalies.remove(anomaly);
        refreshUi();
    }
}