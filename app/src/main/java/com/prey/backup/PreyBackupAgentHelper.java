package com.prey.backup;

import android.app.backup.BackupAgentHelper;

import com.prey.PreyLogger;

/**
 * Created by oso on 10-05-17.
 */

public class PreyBackupAgentHelper extends BackupAgentHelper{

    @Override
    public void onCreate() {

        PreyLogger.i("PreyBackupAgentHelper");

    }
}
