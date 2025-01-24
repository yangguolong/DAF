package com.superpowered.recorder;

import android.annotation.SuppressLint;

import android.os.Looper;

import android.text.TextUtils;


import androidx.annotation.NonNull;

import com.tencent.mmkv.MMKV;

import java.util.HashMap;

import java.util.Map;


import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;



/**
 * @author li
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        String rootDir = MMKV.initialize(this);
        System.out.println("MMKV 初始化路径: " + rootDir);


    }
}
