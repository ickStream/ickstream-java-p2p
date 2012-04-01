#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <ickDiscoveryJNI.h>
#include "ickDiscovery.h"

JavaVM* gJavaVM = NULL;
jobject gService = NULL;

void onMessage(const char * szDeviceId, const void * message, const size_t messageLength)
{
    JNIEnv *env;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
            return;
        }
    }
    jclass cls = (*env)->GetObjectClass(env, gService);
    jmethodID onMessageID = (*env)->GetMethodID(env, cls, "onMessage", "(Ljava/lang/String;Ljava/lang/String;)V");
    jstring messageJava = (*env)->NewStringUTF(env, message);
    jstring deviceJava = (*env)->NewStringUTF(env, szDeviceId);
    (*env)->CallVoidMethod(env, gService, onMessageID, deviceJava, messageJava);
    (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

void onDevice(const char * szDeviceId, enum ickDiscovery_command change, enum ickDevice_servicetype type)
{
    JNIEnv *env;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
            return;
        }
    }
    jclass cls = (*env)->GetObjectClass(env, gService);
    jmethodID onDeviceID = (*env)->GetMethodID(env, cls, "onDevice", "(Ljava/lang/String;int;int;)V");
    jstring deviceJava = (*env)->NewStringUTF(env, szDeviceId);
    (*env)->CallVoidMethod(env, gService, onDeviceID, deviceJava, change, type);
    (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    gJavaVM = vm;
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        puts("OnLoad failure");
        return -1;
    }
    puts("OnLoad successful");
    return JNI_VERSION_1_4;
}

void Java_com_ickstream_common_ickdiscovery_IckDiscovery_initDiscovery(JNIEnv * env, jobject service, jstring deviceIdJava, jstring interfaceJava)
{
    puts("Discovery started");
    gService = (*env)->NewGlobalRef(env, service);
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szInterface = (*env)->GetStringUTFChars(env, interfaceJava, NULL);
    ickDeviceRegisterMessageCallback(&onMessage);
    ickDeviceRegisterDeviceCallback(&onDevice);
    ickInitDiscovery(szDeviceId, szInterface);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    (*env)->ReleaseStringUTFChars(env, interfaceJava, szInterface);
}

void Java_com_ickstream_common_ickdiscovery_IckDiscovery_endDiscovery(JNIEnv * env, jobject service)
{
    ickEndDiscovery(1);
    (*env)->DeleteGlobalRef(env, gService);
    gService = NULL;
    puts("Discovery stopped");
}

void Java_com_ickstream_common_ickdiscovery_IckDiscovery_addService(JNIEnv * env, jobject this, jint type)
{
    ickDiscoveryAddService(type);
}

void Java_com_ickstream_common_ickdiscovery_IckDiscovery_removeService(JNIEnv * env, jobject this, jint type)
{
    ickDiscoveryRemoveService(type);
}

jobjectArray Java_com_ickstream_common_ickdiscovery_IckDiscovery_getDeviceList(JNIEnv * env, jobject this, jint type)
{
    char** devices = ickDeviceList(type);
    int size=0;
    char** current = devices;
    while(current != NULL) {
        size=size+1;
        current = current + 1;
    }
    current = devices;
    jobjectArray result;
    result = (*env)->NewObjectArray(env, size, (*env)->FindClass(env, "java/lang/String"), (*env)->NewStringUTF(env, ""));
    if(result == NULL) {
        return NULL;
    }
    int i;
    for(i=0;i<size;i++) {
        (*env)->SetObjectArrayElement(env, result, i, (*env)->NewStringUTF(env, devices[i]));
    }
    return result;
}

jint Java_com_ickstream_common_ickdiscovery_IckDiscovery_getDeviceType(JNIEnv * env, jobject this, jstring deviceIdJava)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    jint type = ickDeviceType(szDeviceId);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    return type;
}

void Java_com_ickstream_common_ickdiscovery_IckDiscovery_sendMessage(JNIEnv * env, jobject this, jstring deviceIdJava, jstring messageJava)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szMessage = (*env)->GetStringUTFChars(env, messageJava, NULL);

    size_t messageLength = (*env)->GetStringUTFLength(env, messageJava);

    ickDeviceSendMsg(szDeviceId, szMessage, messageLength);

    (*env)->ReleaseStringUTFChars(env, messageJava, szMessage);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
}



