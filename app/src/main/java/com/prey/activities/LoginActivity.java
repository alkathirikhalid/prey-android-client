/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import com.prey.PreyLogger;
import com.prey.PreyVerify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;

import com.prey.PreyConfig;
import com.prey.actions.location.PreyLocation;
import com.prey.services.PreyArpService;
import com.prey.services.PreyDisablePowerOptionsService;
import com.prey.services.PreyLockService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class LoginActivity extends Activity {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Delete notifications (in case Activity was started by one of them)

        startup();
/*
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("/proc/net/arp")));

            String line;
            while((line = br.readLine()) != null) {
                //total += line + "\n";
                PreyLogger.i("line:"+line);
            }

        } catch (Exception e) {

        }*/

        Intent intent = new Intent(this, PreyArpService.class);
        this.startService(intent);


        boolean disablePowerOptions = PreyConfig.getPreyConfig(getApplicationContext()).isDisablePowerOptions();
        if (disablePowerOptions) {
            startService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
        } else {
            stopService(new Intent(getApplicationContext(), PreyDisablePowerOptionsService.class));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        startup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startup();
    }

    private void startup() {
        Intent intent = null;
        boolean ready=PreyConfig.getPreyConfig(this).getProtectReady();
        if (isThisDeviceAlreadyRegisteredWithPrey()) {
            PreyVerify.getInstance(this);
        }
        if (isThereBatchInstallationKey()&&!ready) {
                showLoginBatch();
        } else {
                showLogin();
        }
    }

    private void showLogin() {
        Intent intent = null;
        intent = new Intent(LoginActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void showLoginBatch() {
        Intent intent = null;
        intent = new Intent(LoginActivity.this, WelcomeBatchActivity.class);
        startActivity(intent);
        finish();
    }


    private boolean isThisDeviceAlreadyRegisteredWithPrey() {
        return PreyConfig.getPreyConfig(LoginActivity.this).isThisDeviceAlreadyRegisteredWithPrey(false);
    }

    private void showFeedback(Context ctx) {
        Intent popup = new Intent(ctx, FeedbackActivity.class);
        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(popup);
    }

    private boolean isThereBatchInstallationKey() {
        String apiKeyBatch = PreyConfig.getPreyConfig(LoginActivity.this).getApiKeyBatch();
        return (apiKeyBatch != null && !"".equals(apiKeyBatch));
    }

}

