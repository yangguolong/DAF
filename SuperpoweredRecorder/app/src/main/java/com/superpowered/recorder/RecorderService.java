package com.superpowered.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.superpowered.recorder.bus.RxBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RecorderService extends Service {
    public static final String CHANNELID = "RecorderServiceChannel";
    Disposable progressEvent;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ((action != null) && action.equals("stop")) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNELID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNELID).setContentTitle("Recorder Service").build();
        startForeground(1, notification);

        // Get the device's sample rate and buffer size to enable
        // low-latency Android audio output, if available.
        String samplerateString = null, buffersizeString = null;
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            samplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            buffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }
        if (samplerateString == null) samplerateString = "48000";
        if (buffersizeString == null) buffersizeString = "480";
        int samplerate = Integer.parseInt(samplerateString);
        int buffersize = Integer.parseInt(buffersizeString);

        System.loadLibrary("RecorderExample");  // Load native library.
         int volume = intent.getIntExtra(AudioConfiguration.KEY_VOLUME,20);
        // 麦克风灵敏度（范围：0~100，默认30）
         int micSensitivity = intent.getIntExtra(AudioConfiguration.KEY_MICRO_SENSITIVITY,30);
        // 延迟时间（单位：毫秒，默认100ms）
        int latencyTime = intent.getIntExtra(AudioConfiguration.KEY_LATENCY_TIME, 100);
        StartAudio(samplerate, buffersize, volume, micSensitivity, latencyTime);
        register();
        return START_NOT_STICKY;
    }

    private void register(){
        progressEvent= RxBus.getDefault().toObservable(AudioConfiguration.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<AudioConfiguration>() {
                    @Override
                    public void accept(AudioConfiguration event) throws Exception {
                        SetConfig(event.volume,event.micSensitivity,event.latencyTime);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StopRecording();
        if (progressEvent != null) {
            progressEvent.dispose();
            progressEvent = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Functions implemented in the native library.
    private native void StartAudio(int samplerate, int buffersize, int volume, int micSensitivity, int latencyTime);

    private native void StopRecording();

    private native void SetConfig(int volume, int micSensitivity, int latencyTime);
}
