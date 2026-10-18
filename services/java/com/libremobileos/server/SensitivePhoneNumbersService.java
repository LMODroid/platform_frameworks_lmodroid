/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;

import com.android.server.SystemService;
import com.android.server.telecom.ISensitivePhoneNumbers;

import com.libremobileos.app.LineageContextConstants;

import org.lineageos.lib.phone.SensitivePhoneNumbers;

/** @hide */
public class SensitivePhoneNumbersService extends SystemService {
    private final Context mContext;

    public SensitivePhoneNumbersService(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            return;
        }
        publishBinderService(
                LineageContextConstants.LINEAGE_SENSITIVE_PHONE_NUMBERS_SERVICE, mService);
    }

    private final IBinder mService = new ISensitivePhoneNumbers.Stub() {
        @Override
        public boolean isSensitiveNumber(String number, int subId) {
            final long token = Binder.clearCallingIdentity();
            try {
                return SensitivePhoneNumbers.getInstance()
                        .isSensitiveNumber(mContext, number, subId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    };
}
