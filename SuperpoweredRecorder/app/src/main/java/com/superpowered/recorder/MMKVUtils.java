package com.superpowered.recorder;

import com.tencent.mmkv.MMKV;

public class MMKVUtils {
    private static MMKV mmkv;

    // 获取默认实例（单例模式）
    private static MMKV getMMKV() {
        if (mmkv == null) {
            mmkv = MMKV.defaultMMKV();
        }
        return mmkv;
    }

    // 存储数据（直接提交）
    public static void put(String key, Object value) {
        if (value == null) {
            remove(key);
            return;
        }

        if (value instanceof String) {
            getMMKV().putString(key, (String) value);
        } else if (value instanceof Integer) {
            getMMKV().putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            getMMKV().putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            getMMKV().putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            getMMKV().putLong(key, (Long) value);
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }

    // 读取数据（带默认值）
    public static String getString(String key, String defaultValue) {
        return getMMKV().getString(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return getMMKV().getInt(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getMMKV().getBoolean(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue) {
        return getMMKV().getFloat(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return getMMKV().getLong(key, defaultValue);
    }

    // 移除数据
    public static void remove(String key) {
        getMMKV().removeValueForKey(key);
    }

    // 清空所有数据
    public static void clear() {
        getMMKV().clearAll();
    }

    // 检查是否包含某个key
    public static boolean contains(String key) {
        return getMMKV().containsKey(key);
    }
}