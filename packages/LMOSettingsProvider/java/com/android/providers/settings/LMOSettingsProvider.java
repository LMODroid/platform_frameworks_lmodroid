/**
 * Copyright (C) 2023 The LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.providers.settings;

import android.Manifest;
import android.os.Process;
import android.os.UserManager;
import android.os.UserHandle;
import android.os.RemoteException;
import android.content.Context;
import android.util.Slog;
import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.UserInfo;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.android.providers.settings.SettingsState.Setting;

import com.libremobileos.providers.LMOSettings;

public final class LMOSettingsProvider {
    private static final String LOG_TAG = "LMOSettingsProvider";

    public static void onPreUpgradeLocked(int userId, Context context, SettingsState systemSettings, SettingsState secureSettings, SettingsState globalSettings) {
        final int latestVersion = 2;
        Setting versionSetting = secureSettings.getSettingLocked(
                "lmo_db_ver");
        boolean willUpgradeGlobal = userId == UserHandle.USER_SYSTEM;
        int currentVersion = 0;
        if (!versionSetting.isNull()) {
            try {
                currentVersion = Integer.valueOf(versionSetting.getValue());
            } catch (NumberFormatException unused) {
            }
        }

        if (currentVersion == 0) {
            Setting currentSetting = systemSettings.getSettingLocked(
                    "transistent_task_mode");
            if (!currentSetting.isNull()) {
                systemSettings.insertSettingOverrideableByRestoreLocked(
                        LMOSettings.System.TRANSIENT_TASK_MODE,
                        currentSetting.getValue(),
                        null, true, SettingsState.SYSTEM_PACKAGE_NAME);
                systemSettings.deleteSettingLocked("transistent_task_mode");
            }

            Setting forceNavBarSetting = systemSettings.getSettingLocked(
                    LMOSettings.System.FORCE_SHOW_NAVBAR);
            if (forceNavBarSetting.isNull()) {
                String defaultForceNavBar = Integer.toString(context
                        .getResources().getInteger(R.integer.def_force_show_navbar));
                globalSettings.insertSettingOverrideableByRestoreLocked(
                        LMOSettings.System.FORCE_SHOW_NAVBAR, defaultForceNavBar,
                        null, true, SettingsState.SYSTEM_PACKAGE_NAME);
            }

            Setting clockPositionSetting = systemSettings.getSettingLocked(
                    LMOSettings.System.STATUS_BAR_CLOCK);
            if (clockPositionSetting.isNull()) {
                String defaultClockPosition = Integer.toString(context
                        .getResources().getInteger(R.integer.def_clock_position));
                globalSettings.insertSettingOverrideableByRestoreLocked(
                        LMOSettings.System.STATUS_BAR_CLOCK, defaultClockPosition,
                        null, true, SettingsState.SYSTEM_PACKAGE_NAME);
            }

            Setting volumePanelSetting = secureSettings.getSettingLocked(
                    LMOSettings.Secure.VOLUME_PANEL_ON_LEFT);
            if (volumePanelSetting.isNull()) {
                secureSettings.insertSettingOverrideableByRestoreLocked(
                        LMOSettings.Secure.VOLUME_PANEL_ON_LEFT,
                        context.getResources().getBoolean(
                                R.bool.def_volume_panel_on_left) ? "1" : "0",
                        null, true, SettingsState.SYSTEM_PACKAGE_NAME);
            }

            currentVersion = 1;
        }

        if (currentVersion == 1) {
            if (willUpgradeGlobal) {
                Setting currentSetting = globalSettings.getSettingLocked(
                        Settings.Global.PRIVATE_DNS_MODE);
                if (!currentSetting.isNull()
                        && "cloudflare".equals(currentSetting.getValue())) {
                    globalSettings.insertSettingOverrideableByRestoreLocked(
                            Settings.Global.PRIVATE_DNS_SPECIFIER,
                            "one.one.one.one",
                            null, true, SettingsState.SYSTEM_PACKAGE_NAME);
                    globalSettings.insertSettingOverrideableByRestoreLocked(
                            Settings.Global.PRIVATE_DNS_MODE,
                            "hostname",
                            null, true, SettingsState.SYSTEM_PACKAGE_NAME);
                }
            }
            currentVersion = 2;
        }

        if (currentVersion != latestVersion) {
            Slog.wtf("onPreUpgradeLocked", currentVersion + " found, expected " + latestVersion);
        } else {
            secureSettings.insertSettingLocked(
                    "lmo_db_ver", String.valueOf(currentVersion),
                    null, true, SettingsState.SYSTEM_PACKAGE_NAME);
        }
    }

    public static void loadRestrictedNetworkingModeSetting(Context context, SettingsState globalSettings) {
        globalSettings.insertSettingLocked(
            Global.RESTRICTED_NETWORKING_MODE, "1", null, false,
            SettingsState.SYSTEM_PACKAGE_NAME);
        try {
            List<PackageInfo> packages = new ArrayList<>();
            for (UserInfo userInfo : UserManager.get(context).getAliveUsers()) {
                packages.addAll(
                        AppGlobals.getPackageManager().getPackagesHoldingPermissions(
                                new String[]{Manifest.permission.INTERNET},
                                PackageManager.MATCH_UNINSTALLED_PACKAGES,
                                userInfo.id
                        ).getList());
            }
            final StringJoiner joiner = new StringJoiner(";");
            packages.forEach(packageInfo -> {
                int uid = packageInfo.applicationInfo.uid;
                if (uid < 0 || UserHandle.getAppId(uid) > Process.LAST_APPLICATION_UID) {
                    throw new IllegalArgumentException("Invalid uid");
                }
                joiner.add(String.valueOf(uid));
            });
            globalSettings.insertSettingLocked(
                    "uids_allowed_on_restricted_networks", joiner.toString(), null, false,
                    SettingsState.SYSTEM_PACKAGE_NAME);
        } catch (RemoteException e) {
            Slog.e(LOG_TAG, "Failed to set uids allowed on restricted networks");
        }
    }

}
