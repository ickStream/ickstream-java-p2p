//
//  ickDiscoveryJNI.c
//  ickDiscovery JNI Wrapper
//
// Copyright (C) 2013 ickStream GmbH
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

void onMessage(const char * szSourceDeviceId, const char * message, size_t messageLength, enum ickMessage_communicationstate state, ickDeviceServicetype_t service_type, const char * szTargetDeviceId)
{
#ifdef DEBUG
#ifdef __ANDROID_API__
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "ickDevice_message_callback_t(%s,%s,%d,%d,%d,%p=%s)\n",szSourceDeviceId,message,messageLength,state,service_type,szTargetDeviceId,szTargetDeviceId);
#else
    printf("ickDevice_message_callback_t(%s,%s,%d,%d,%d,%p=%s)\n",szSourceDeviceId,message,messageLength,state,service_type,szTargetDeviceId,szTargetDeviceId);
#endif
#endif
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
        jmethodID onMessageID = (*env)->GetMethodID(env, cls, "onNativeMessage", "(Ljava/lang/String;Ljava/lang/String;I[B)V");
        if(onMessageID != NULL) {
            jbyteArray messageJava = (*env)->NewByteArray(env, messageLength);
            (*env)->SetByteArrayRegion(env, messageJava, 0, messageLength, message);
            jstring sourceDeviceJava = (*env)->NewStringUTF(env, szSourceDeviceId);
            jstring targetDeviceJava = (*env)->NewStringUTF(env, szTargetDeviceId);
            jint javaServiceType = service_type;
            (*env)->CallVoidMethod(env, gService, onMessageID, sourceDeviceJava, targetDeviceJava, javaServiceType, messageJava);
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
    ickDiscoverySetLogFacility(&log);
#ifdef DEBUG
    freopen("/sdcard/ickStreamPlayer.log", "w", stderr);
#endif
#endif

    return JNI_VERSION_1_4;
}

jint Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_nativeInitDiscovery(JNIEnv * env, jobject service, jstring deviceIdJava, jstring interfaceJava, jstring deviceNameJava, jstring dataFolderJava)
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

    ickDiscoveryResult_t result = ickInitDiscovery(szDeviceId, szInterface,NULL);
    ickDiscoverySetupConfigurationData(szDeviceName, NULL);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    if(szDeviceName != NULL) {
    	(*env)->ReleaseStringUTFChars(env, deviceNameJava, szDeviceName);
    }
    //if(szDataFolder != NULL) {
    //	(*env)->ReleaseStringUTFChars(env, dataFolderJava, szDataFolder);
    //}
    (*env)->ReleaseStringUTFChars(env, interfaceJava, szInterface);
    return result;
}

void Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_endDiscovery(JNIEnv * env, jobject service)
{
    ickEndDiscovery(1);
    (*env)->DeleteGlobalRef(env, gService);
    gService = NULL;
}

jint Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_addService(JNIEnv * env, jobject this, jint type)
{
    return ickDiscoveryAddService(type);
}

jint Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_removeService(JNIEnv * env, jobject this, jint type)
{
    return ickDiscoveryRemoveService(type);
}

int Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_getDevicePort(JNIEnv * env, jobject this, jstring deviceIdJava)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    int port = ickDevicePort(szDeviceId);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    return port;
}

jstring Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_getDeviceAddress(JNIEnv * env, jobject this, jstring deviceIdJava)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szURL = ickDeviceURL(szDeviceId);
    jstring url = (*env)->NewStringUTF(env, szURL);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    return url;
}

jstring Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_getDeviceName(JNIEnv * env, jobject this, jstring deviceIdJava)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szName = ickDeviceName(szDeviceId);
    jstring name = (*env)->NewStringUTF(env, szName);
    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    return name;
}

jboolean Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_nativeSendMessage(JNIEnv * env, jobject this, jstring sourceDeviceIdJava, jstring targetDeviceIdJava, jbyteArray messageJava)
{
    const char * szTargetDeviceId = NULL;
    if (targetDeviceIdJava != NULL) {
        szTargetDeviceId = (*env)->GetStringUTFChars(env, targetDeviceIdJava, NULL);
    }

    const char * szSourceDeviceId = NULL;
    if (sourceDeviceIdJava != NULL) {
        szSourceDeviceId = (*env)->GetStringUTFChars(env, sourceDeviceIdJava, NULL);
    }

    jbyte* byteMessage = (*env)->GetByteArrayElements(env, messageJava, NULL);
    jsize messageLength = (*env)->GetArrayLength(env, messageJava);

    int result = JNI_FALSE;
    int i=0;
    while(i<10) {
#ifdef DEBUG
#ifdef __ANDROID_API__
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "ickDeviceSendMsg(%s,%s,%d)\n",szTargetDeviceId,byteMessage,messageLength);
#else
        printf("ickDeviceSendMsg(%s,%s,%d)\n",szTargetDeviceId,byteMessage,messageLength);
#endif
#endif
        if(ickDeviceSendMsg(szTargetDeviceId, byteMessage, messageLength) == ICKMESSAGE_SUCCESS) {
            result = JNI_TRUE;
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
    if (szTargetDeviceId != NULL) {
        (*env)->ReleaseStringUTFChars(env, targetDeviceIdJava, szTargetDeviceId);
    }
    if (szSourceDeviceId != NULL) {
        (*env)->ReleaseStringUTFChars(env, sourceDeviceIdJava, szSourceDeviceId);
    }
    return result;
}

jboolean Java_com_ickstream_common_ickdiscovery_IckDiscoveryJNI_nativeSendTargetedMessage(JNIEnv * env, jobject this, jstring sourceDeviceIdJava, jstring targetDeviceIdJava, jint targetServiceType, jbyteArray messageJava)
{
    const char * szTargetDeviceId = NULL;
    if (targetDeviceIdJava != NULL) {
        szTargetDeviceId = (*env)->GetStringUTFChars(env, targetDeviceIdJava, NULL);
    }

    const char * szSourceDeviceId = NULL;
    if (sourceDeviceIdJava != NULL) {
        szSourceDeviceId = (*env)->GetStringUTFChars(env, sourceDeviceIdJava, NULL);
    }

    jbyte* byteMessage = (*env)->GetByteArrayElements(env, messageJava, NULL);
    jsize messageLength = (*env)->GetArrayLength(env, messageJava);

    int result = JNI_FALSE;
    int i=0;
    while(i<10) {
#ifdef DEBUG
#ifdef __ANDROID_API__
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "ickDeviceSendTargetedMsg(%s,%s,%d,%d,%s)\n",szTargetDeviceId,byteMessage,messageLength,targetServiceType, szSourceDeviceId);
#else
        printf("ickDeviceSendTargetedMsg(%s,%s,%d,%d,%s)\n",szTargetDeviceId,byteMessage,messageLength,targetServiceType, szSourceDeviceId);
#endif
#endif
        if(ickDeviceSendTargetedMsg(szTargetDeviceId, byteMessage, messageLength, targetServiceType, szSourceDeviceId) == ICKMESSAGE_SUCCESS) {
            result = JNI_TRUE;
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
    if (szTargetDeviceId != NULL) {
        (*env)->ReleaseStringUTFChars(env, targetDeviceIdJava, szTargetDeviceId);
    }
    if (szSourceDeviceId != NULL) {
        (*env)->ReleaseStringUTFChars(env, sourceDeviceIdJava, szSourceDeviceId);
    }
    return result;
}

#ifdef __ANDROID_API__
void log( const char *file, int line, int prio, const char * format, ... )
{
    va_list argptr;
    va_start(argptr,format);
    switch(prio) {
        case LOG_EMERG:
        case LOG_ALERT:
            __android_log_vprint(ANDROID_LOG_FATAL,DEBUG_TAG,format,argptr);
            break;
        case LOG_CRIT:
        case LOG_ERR:
            __android_log_vprint(ANDROID_LOG_ERROR,DEBUG_TAG,format,argptr);
            break;
        case LOG_WARNING:
            __android_log_vprint(ANDROID_LOG_WARN,DEBUG_TAG,format,argptr);
            break;
        case LOG_NOTICE:
        case LOG_INFO:
            __android_log_vprint(ANDROID_LOG_INFO,DEBUG_TAG,format,argptr);
            break;
        case LOG_DEBUG:
        default:
            __android_log_vprint(ANDROID_LOG_DEBUG,DEBUG_TAG,format,argptr);
            break;
    }
    va_end(argptr);
}
#endif
