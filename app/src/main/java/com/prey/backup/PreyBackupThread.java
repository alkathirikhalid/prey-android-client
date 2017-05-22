package com.prey.backup;

import android.app.backup.BackupAgentHelper;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.prey.PreyAccountData;
import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPhone;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.PermissionInformationActivity;
import com.prey.activities.WelcomeBatchActivity;
import com.prey.exceptions.PreyException;
import com.prey.net.PreyWebServices;

/**
 * Created by oso on 10-05-17.
 */

public class PreyBackupThread extends Thread {


    private Context ctx;

    public PreyBackupThread(Context ctx) {
        this.ctx = ctx;
        PreyLogger.i("PreyBackupThread");
    }

    public void run() {
        try {
            PreyLogger.i("PreyBackupThread run");
            String deviceId = PreyConfig.getPreyConfig(ctx).getDeviceId();
            String apiKey = PreyConfig.getPreyConfig(ctx).getApiKey();
            String email = PreyConfig.getPreyConfig(ctx).getEmail();
            PreyLogger.i("deviceId:" + deviceId);
            PreyLogger.i("apikey:" + apiKey);
            PreyPhone phone = new PreyPhone(ctx);
            PreyPhone.Hardware hardware = phone.getHardware();
            String uuid = hardware.getUuid();
            PreyLogger.i("uuid:" + uuid);
            if (uuid != null && !"".equals(uuid)) {
                String uuidOld = PreyWebServices.getInstance().getUuidDevice(ctx);
                PreyLogger.i("uuidOld:" + uuidOld);
                if (uuidOld != null && !"".equals(uuidOld) && !uuid.equals(uuidOld)) {
                    PreyAccountData accountData = PreyWebServices.getInstance().registerNewDeviceWithApiKeyEmail(ctx, apiKey, email, PreyUtils.getDeviceType(ctx));

                    PreyLogger.i("new deviceId:" + accountData.getDeviceId());
                    PreyLogger.i("new apikey:" + accountData.getApiKey());
                    PreyConfig.getPreyConfig(ctx).saveAccount(accountData);
                    PreyConfig.getPreyConfig(ctx).registerC2dm();
                    PreyWebServices.getInstance().sendEvent(ctx, PreyConfig.ANDROID_SIGN_UP);

                    PreyLogger.i("get deviceId:" + PreyConfig.getPreyConfig(ctx).getDeviceId());
                    PreyLogger.i("get apikey:" + PreyConfig.getPreyConfig(ctx).getApiKey());
                }
            }
        }catch (Exception e){

        }

    }

}
