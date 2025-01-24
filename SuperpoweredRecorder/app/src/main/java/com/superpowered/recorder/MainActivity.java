package com.superpowered.recorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.superpowered.recorder.bus.RxBus;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private boolean recording = false;
    AudioConfiguration audioConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.startStop);
        b.setVisibility(View.GONE);
        // 音量（范围：0~30，默认20）
        int volume = MMKVUtils.getInt(AudioConfiguration.KEY_VOLUME, 20);
        // 麦克风灵敏度（范围：0~100，默认30）
        int micSensitivity = MMKVUtils.getInt(AudioConfiguration.KEY_MICRO_SENSITIVITY, 30);
        // 延迟时间（单位：毫秒，默认100ms）
        int latencyTime = MMKVUtils.getInt(AudioConfiguration.KEY_LATENCY_TIME, 100);

        audioConfiguration = new AudioConfiguration(volume, micSensitivity, latencyTime);
        // Checking permissions.
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO
        };
        for (String s:permissions) {
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                // Some permissions are not granted, ask the user.
                ActivityCompat.requestPermissions(this, permissions, 0);
                return;
            }
        }

        // Got all permissions, show button.
        showButton();
        initFanListener();
    }
    private void initFanListener() {
        FanProgressBar fpb = findViewById(R.id.fpb);
        fpb.setStyle("ms", "0", "500", 0, 500);

        fpb.setListener(new FanProgressBar.Listener() {
            
            @Override
            public void onDown(boolean isProgress) {
            }

            @Override
            public void onChange(boolean isProgress, int now) {
                audioConfiguration.latencyTime = now;
                postEvenBus();
            }

            @Override
            public void onUp(boolean isProgress, int now) {
                audioConfiguration.latencyTime = now;
                MMKVUtils.put(AudioConfiguration.KEY_LATENCY_TIME, now);
                postEvenBus();
            }
        });
        fpb.setProgress(audioConfiguration.latencyTime);
    }

    private void postEvenBus() {
        RxBus.getDefault().post(audioConfiguration);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Called when the user answers to the permission dialogs.
        if ((requestCode != 0) || (grantResults.length < 1) || (grantResults.length != permissions.length)) return;
        boolean hasAllPermissions = true;

        for (int grantResult:grantResults) if (grantResult != PackageManager.PERMISSION_GRANTED) {
            hasAllPermissions = false;
            Toast.makeText(getApplicationContext(), "Please allow all permissions for the app.", Toast.LENGTH_LONG).show();
        }

        if (hasAllPermissions) showButton();
    }

    private void showButton() {
        Button b = findViewById(R.id.startStop);
        b.setVisibility(View.VISIBLE);
    }

    private void updateButton() {
        Button b = findViewById(R.id.startStop);
        b.setText(recording ? "Stop" : "Start");
    }

    // Handle Start/Stop button toggle.
    public void ToggleStartStop(View button) {
        Intent serviceIntent = new Intent(this, RecorderService.class);
        if (recording) {
            serviceIntent.setAction("stop");
            startService(serviceIntent);
            recording = false;
        } else {
            serviceIntent.putExtra(AudioConfiguration.KEY_VOLUME,audioConfiguration.volume);
            serviceIntent.putExtra(AudioConfiguration.KEY_MICRO_SENSITIVITY,audioConfiguration.micSensitivity);
            serviceIntent.putExtra(AudioConfiguration.KEY_LATENCY_TIME,audioConfiguration.latencyTime);
            ContextCompat.startForegroundService(this, serviceIntent);
            recording = true;
        }
        updateButton();
    }
}
