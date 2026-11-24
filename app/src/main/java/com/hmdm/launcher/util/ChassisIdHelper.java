package com.hmdm.launcher.util;

import android.util.Log;

import com.hmdm.launcher.BuildConfig;
import com.hmdm.launcher.Const;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Reads the robot serial from the AutoXing chassis local API:
 * GET {CHASSIS_URL}/device/info -> { "device": { "sn": "..." } }
 * Same call as ChassisClient.fetchIdentity() in ctrl_robot_bridge.
 * No Secret header needed: the tablet is on 192.168.25.*, which the chassis trusts.
 */
public class ChassisIdHelper {
    private static final int TIMEOUT_MS = 3000;
    private static final int MAX_ATTEMPTS = 4;        // chassis answers 503 while booting
    private static final long RETRY_DELAY_MS = 2000;

    /** Blocking. Call from a background thread. Returns the robot serial or null. */
    public static String fetchRobotId() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(BuildConfig.CHASSIS_URL + "/device/info").openConnection();
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                int code = conn.getResponseCode();
                if (code == 503) {
                    Log.d(Const.LOG_TAG, "Chassis still booting, retrying robot ID fetch");
                } else if (code == 200) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = r.readLine()) != null) sb.append(line);
                    }
                    JSONObject device = new JSONObject(sb.toString()).optJSONObject("device");
                    String sn = device != null ? device.optString("sn", "").trim() : "";
                    Log.i(Const.LOG_TAG, "Robot ID from chassis: " + (sn.isEmpty() ? "<empty>" : sn));
                    return sn.isEmpty() ? null : sn;
                } else {
                    Log.d(Const.LOG_TAG, "Chassis /device/info returned HTTP " + code);
                    return null;
                }
            } catch (Exception e) {
                // Not a robot / chassis unreachable: leave the field blank
                Log.d(Const.LOG_TAG, "Robot ID fetch failed: " + e.getMessage());
                return null;
            } finally {
                if (conn != null) conn.disconnect();
            }
            try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException e) { return null; }
        }
        return null;
    }
}
