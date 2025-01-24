package com.superpowered.recorder;

public class AudioConfiguration {
    public static final String KEY_VOLUME = "KEY_VOLUME";
    public static final String KEY_MICRO_SENSITIVITY = "KEY_MICRO_SENSITIVITY";
    public static final String KEY_LATENCY_TIME= "KEY_LATENCY_TIME";
    // 音量（范围：0~30，默认20）
    public int volume = 20;
    // 麦克风灵敏度（范围：0~100，默认30）
    public int micSensitivity = 30;
    // 延迟时间（单位：毫秒，默认100ms）
    public int latencyTime = 100;
    public AudioConfiguration() {}

    /**
     * 全参数构造函数
     * @param volume 音量值
     * @param micSensitivity 麦克风灵敏度
     * @param latencyTime 延迟时间
     */
    public AudioConfiguration(int volume, int micSensitivity, int latencyTime) {
        this.volume = volume;
        this.micSensitivity = micSensitivity;
        this.latencyTime = latencyTime;
    }
}
