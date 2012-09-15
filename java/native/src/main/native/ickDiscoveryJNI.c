//
//  ickDiscoveryJNI.c
//  ickDiscovery JNI Wrapper
//
// Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
// All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#ifdef __ANDROID_API__
#include <android/log.h>
#endif
#include "ickDiscovery.h"

JavaVM* gJavaVM = NULL;
jobject gService = NULL;
char * myDeviceId = NULL;

void onMessage(const char * szDeviceId, const void * message, size_t messageLength, enum ickMessage_communicationstate state)
{
    int attached = 0;
    JNIEnv *env;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
#ifdef __ANDROID_API__
            __android_log_print(ANDROID_LOG_ERROR, DEBUG_TAG, "Failed to get the environment or attach thread");
#else
            puts("Failed to get the environment or attach thread");
#endif
            return;
        }
        attached = 1;
    }
    if(gService != NULL) {
        jclass cls = (*env)->GetObjectClass(env, gService);
        jmethodID onMessageID = (*env)->GetMethodID(env, cls, "onMessage", "(Ljava/lang/String;[B)V");
        if(onMessageID != NULL) {
            jbyteArray messageJava = (*env)->NewByteArray(env, messageLength);
            (*env)->SetByteArrayRegion(env, messageJava, 0, messageLength, message);
            jstring deviceJava = (*env)->NewStringUTF(env, szDeviceId);
            (*env)->CallVoidMethod(env, gService, onMessageID, deviceJava, messageJava);
        }
    }
    if(attached) {
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
    }
}

void onDevice(const char * szDeviceId, enum ickDiscovery_command change, enum ickDevice_servicetype type)
{
    JNIEnv *env;
    int attached = 0;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
#ifdef __ANDROID_API__
            __android_log_print(ANDROID_LOG_ERROR, DEBUG_TAG, "Failed to get the environment or attach thread");
#else
            puts("Failed to get the environment or attach thread");
#endif
            return;
        }
        attached = 1;
    }

    if(gService != NULL) {
        jclass cls = (*env)->GetObjectClass(env, gService);
        jmethodID onDeviceID = (*env)->GetMethodID(env, cls, "onDevice", "(Ljava/lang/String;II)V");
        if(onDeviceID != NULL) {
            jstring deviceJava = (*env)->NewStringUTF(env, szDeviceId);
            jint changeJava = change;
            jint typeJava = type;
            (*env)->CallVoidMethod(env, gService, onDeviceID, deviceJava, changeJava, typeJava);
        }else {
            puts("GetMethodID failed");
        }
    }
    if(attached) {
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
    }
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    gJavaVM = vm;
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
#ifdef __ANDROID_API__
        __android_log_print(ANDROID_LOG_ERROR, DEBUG_TAG, "Failed to get the environment or attach thread");
#else
            puts("Failed to get the environment or attach thread");
#endif
        return -1;
    }
#ifdef __ANDROID_API__
#ifdef DEBUG
    freopen("/sdcard/ickStreamPlayer.log", "w", stderr);
#endif
#endif

    return JNI_VERSION_1_4;
}

void Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_initDiscovery(JNIEnv * env, jobject service, jstring deviceIdJava, jstring interfaceJava, jstring deviceNameJava, jstring dataFolderJava)
{
    gService = (*env)->NewGlobalRef(env, service);
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szDeviceName = (*env)->GetStringUTFChars(env, deviceNameJava, NULL);
    //const char * szDataFolder = (*env)->GetStringUTFChars(env, dataFolderJava, NULL);
    const char * szInterface = (*env)->GetStringUTFChars(env, interfaceJava, NULL);
    ickDeviceRegisterMessageCallback(&onMessage);
    ickDeviceRegisterDeviceCallback(&onDevice);

    if(myDeviceId != NULL) {
        free(myDeviceId);
    }
    myDeviceId = malloc(strlen(szDeviceId)+1);
    strcpy(myDeviceId,szDeviceId);

    ickInitDiscovery(szDeviceId, szInterface,NULL);
    ickDiscoverySetupConfigurationData(szDeviceName, NULL);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    if(szDeviceName != NULL) {
    	(*env)->ReleaseStringUTFChars(env, deviceNameJava, szDeviceName);
    }
    //if(szDataFolder != NULL) {
    //	(*env)->ReleaseStringUTFChars(env, dataFolderJava, szDataFolder);
    //}
    (*env)->ReleaseStringUTFChars(env, interfaceJava, szInterface);
}

void Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_endDiscovery(JNIEnv * env, jobject service)
{
    ickEndDiscovery(1);
    (*env)->DeleteGlobalRef(env, gService);
    gService = NULL;
}

void Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_addService(JNIEnv * env, jobject this, jint type)
{
    ickDiscoveryAddService(type);
}

void Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_removeService(JNIEnv * env, jobject this, jint type)
{
    ickDiscoveryRemoveService(type);
}

jstring Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_getDeviceName(JNIEnv * env, jobject this, jstring deviceIdJava)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szName = ickDeviceName(szDeviceId);
    jstring name = (*env)->NewStringUTF(env, szName);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    return name;
}

void Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_sendMessage(JNIEnv * env, jobject this, jstring deviceIdJava, jbyteArray messageJava)
{
    const char * szDeviceId = NULL;
    if (deviceIdJava != NULL) {
        szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    }

    jbyte* byteMessage = (*env)->GetByteArrayElements(env, messageJava, NULL);
    jsize messageLength = (*env)->GetArrayLength(env, messageJava);

    int i=0;
    while(i<10) {
        if(ickDeviceSendMsg(szDeviceId, byteMessage, messageLength) == ICKMESSAGE_SUCCESS) {
            break;
        }
        sleep(1);
        i++;
    }
    if(i==10) {
#ifdef __ANDROID_API__
        __android_log_print(ANDROID_LOG_ERROR, DEBUG_TAG, "Failed to send message");
#else
        puts("Failed to send message");
        fflush(stdout);
#endif
    }

    (*env)->ReleaseByteArrayElements(env, messageJava, byteMessage, JNI_ABORT);
    if (szDeviceId != NULL) {
        (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    }
}
