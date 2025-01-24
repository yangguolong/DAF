#include <jni.h>
#include <string>
#include <android/log.h>
#include <OpenSource/SuperpoweredAndroidAudioIO.h>
#include <Superpowered.h>
#include <SuperpoweredSimple.h>
#include <SuperpoweredRecorder.h>
#include <unistd.h>
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <vector>
#include <iostream>
#include <cstring>
#include <SuperpoweredDelay.h>
#include <SuperpoweredReverb.h>
static SuperpoweredAndroidAudioIO *audioIO;
static Superpowered::Delay *delay;
static Superpowered::Reverb *reverb;
static int _volume;
static int _micSensitivity;
static int _latencyTime;

// This is called periodically by the audio I/O.
static bool audioProcessing(
        void * __unused clientdata,
        short int *audio,
        int numberOfFrames,
        int __unused samplerate
) {
    // 将输入音频转换为浮点数
    float floatBuffer[numberOfFrames * 2];
    Superpowered::ShortIntToFloat(audio, floatBuffer, (unsigned int)numberOfFrames);
//    reverb->samplerate = (unsigned int)samplerate;
//    reverb->process(floatBuffer, floatBuffer, (unsigned int)numberOfFrames);
    // 设置延迟时间，例如 500 毫秒
    auto delayMs = static_cast<float>(_latencyTime);
    __android_log_print(ANDROID_LOG_DEBUG, "Recorder", "delayMs %f",delayMs);
    delay->delayMs = delayMs;
    // 处理音频
    const float *outputBuffer = delay->process(floatBuffer, numberOfFrames);

    // 在这里，outputBuffer 是延迟处理后的音频输出
    for (unsigned int i = 0; i < numberOfFrames * 2; i++) {
        floatBuffer[i] = outputBuffer[i]; // 将输出信号存到最终输出
    }
    Superpowered::FloatToShortInt(floatBuffer, audio, (unsigned int)numberOfFrames);
    return true;
}


// StartAudio - Start audio engine.
extern "C" JNIEXPORT void
Java_com_superpowered_recorder_RecorderService_StartAudio (
        JNIEnv * __unused env,
        jobject  __unused obj,
        jint samplerate,
        jint buffersize,
        jint volume,
        jint micSensitivity,
        jint latencyTime

) {
    Superpowered::Initialize("ExampleLicenseKey-WillExpire-OnNextUpdate");
    // initialize reverb
    reverb = new Superpowered::Reverb((unsigned int)samplerate);
    reverb->enabled = true;
    _volume = volume;
    _micSensitivity = micSensitivity;
    _latencyTime = latencyTime;

    // 创建 Delay 实例
    const unsigned int maximumDelayMs = 1000; // 最大延迟 1 秒
    const unsigned int maximumSamplerate = 44100; // 采样率 44100Hz
    const unsigned int maximumFramesToProcess = 512; // 每次处理的最大帧数
    // 创建 Delay 对象
    delay = new Superpowered::Delay(maximumDelayMs, maximumSamplerate, maximumFramesToProcess, samplerate);

    // Initialize audio engine with audio callback function.
    audioIO = new SuperpoweredAndroidAudioIO (
            samplerate,      // native sampe rate
            buffersize,      // native buffer size
            true,            // enableInput
            true,           // enableOutput
            audioProcessing, // process callback function
            NULL,
            -1,
            -1             // clientData
    );
}

// StopAudio - Stop audio engine and free audio buffer.
extern "C" JNIEXPORT void
Java_com_superpowered_recorder_RecorderService_StopRecording(JNIEnv * __unused env, jobject __unused obj) {
//    recorder->stop();
    delete audioIO;
    delete delay;
    __android_log_print(ANDROID_LOG_DEBUG, "Recorder", "Finished recording.");

}

extern "C"
JNIEXPORT void JNICALL
Java_com_superpowered_recorder_RecorderService_SetConfig(JNIEnv *env, jobject thiz, jint volume,
                                                         jint mic_sensitivity, jint latency_time) {
    _volume = volume;
    _micSensitivity = mic_sensitivity;
    _latencyTime = latency_time;
}