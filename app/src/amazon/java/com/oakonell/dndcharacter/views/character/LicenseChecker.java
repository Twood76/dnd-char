package com.oakonell.dndcharacter.views.character;

import android.app.Activity;
import android.os.Handler;
import android.provider.Settings;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

/**
 * Created by Rob on 3/14/2016.
 */
public class LicenseChecker {
    public static final int RETRY_REASON = Policy.RETRY;

    private LicenseCallback callback;
    private Activity context;
    private Handler mHandler;

    public interface LicenseCallback {

        void applicationError(int errorCode);

        void allow(int reason);

        void dontAllow(int reason);
    }

    public void onCreate(Activity activity, LicenseCallback callback) {
        mHandler = new Handler();
        this.callback = callback;
        this.context = activity;
    }

    public void doCheck(Activity activity) {
        callback.allow(0);
    }

    public void onDestroy(CharacterActivity characterActivity) {
    }

}
