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

import android.os.UserHandle;
import android.content.Context;
import android.util.Slog;

import com.android.providers.settings.SettingsState.Setting;


public final class LMOSettingsProvider {
    private static final String TAG = "LMOSettingsProvider";

    public static void onPreUpgradeLocked(int userId, Context context, SettingsState systemSettings, SettingsState secureSettings, SettingsState globalSettings) {
        final int latestVersion = 0;
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

        if (currentVersion != latestVersion) {
            Slog.wtf("onPreUpgradeLocked", currentVersion + " found, expected " + latestVersion);
        } else {
            secureSettings.insertSettingLocked(
                    "lmo_db_ver", String.valueOf(currentVersion),
                    null, true, SettingsState.SYSTEM_PACKAGE_NAME);
        }
    }
}
