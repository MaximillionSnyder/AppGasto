#include <jni.h>

extern "C" int gastosec_start_watchdog();
extern "C" int gastosec_is_compromised();

extern "C" JNIEXPORT void JNICALL
Java_com_example_appgasto_security_SecurityBridge_nativeStartWatchdog(JNIEnv*, jclass) {
    gastosec_start_watchdog();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_appgasto_security_SecurityBridge_nativeIsCompromised(JNIEnv*, jclass) {
    return gastosec_is_compromised() ? JNI_TRUE : JNI_FALSE;
}
