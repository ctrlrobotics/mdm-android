package com.hmdm.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.util.Utils;

/**
 * ADB-only escape hatch. Protected by android.permission.DUMP, which only the
 * adb shell / system holds, so regular apps cannot trigger it.
 *
 * adb shell am broadcast -a com.hmdm.launcher.action.RELEASE_DEVICE_OWNER -n com.hmdm.launcher/.receiver.ReleaseDeviceOwnerReceiver
 */
public class ReleaseDeviceOwnerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean released = Utils.releaseDeviceOwner(context);
        String msg = "ReleaseDeviceOwner via ADB: " + (released ? "SUCCESS" : "FAILED");
        Log.i(Const.LOG_TAG, msg);
        setResultCode(released ? 1 : 0);
        setResultData(msg);
    }
}
