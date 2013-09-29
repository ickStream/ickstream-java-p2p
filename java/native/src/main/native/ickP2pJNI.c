//
//  ickP2pJNI.c
//  ickP2p JNI Wrapper
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
#include "ickP2p.h"
#include "libwebsockets.h"

#ifdef DEBUG
#ifdef __ANDROID_API__
#define debug_log( args... ) __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, args)
#else
#define debug_log( args... ) printf(args)
#endif
#else
#define debug_log( args... )
#endif

#ifdef __ANDROID_API__
#define error_log( args... ) __android_log_print(ANDROID_LOG_ERROR, DEBUG_TAG, args)
#else
#define error_log( args... ) puts(args);fflush(stdout);
#endif

JavaVM* gJavaVM = NULL;
jobject gService = NULL;
char * myDeviceId = NULL;
ickP2pContext_t* context = NULL;

void messageCb(ickP2pContext_t *ictx, const char *szSourceDeviceId, ickP2pServicetype_t sourceService, ickP2pServicetype_t targetService, const char* message, size_t messageLength, ickP2pMessageFlag_t mFlags )
{
    debug_log("messageCb(%s,%s,%d,%d,%d)\n",szSourceDeviceId,message,(int)messageLength,sourceService,targetService);

    int attached = 0;
    JNIEnv *env;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
            error_log("Failed to get the environment or attach thread");

            return;
        }
        attached = 1;
    }
    if(gService != NULL) {
        jclass cls = (*env)->GetObjectClass(env, gService);
        jmethodID messageCbID = (*env)->GetMethodID(env, cls, "messageCb", "(Ljava/lang/String;II[B)V");
        if(messageCbID != NULL) {
            jbyteArray messageJava = (*env)->NewByteArray(env, messageLength);
            (*env)->SetByteArrayRegion(env, messageJava, 0, messageLength, message);
            jstring sourceDeviceJava = (*env)->NewStringUTF(env, szSourceDeviceId);
            jint javaTargetService = targetService;
            jint javaSourceService = sourceService;
            (*env)->CallVoidMethod(env, gService, messageCbID, sourceDeviceJava, javaSourceService,javaTargetService, messageJava);
        }
    }
    if(attached) {
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
    }
}

void discoveryCb(ickP2pContext_t *ictx, const char *szDeviceId, ickP2pDeviceState_t change, ickP2pServicetype_t type)
{
    debug_log("discoveryCb(%p,%s,%d,%d)\n",ictx,szDeviceId,change,type);

    JNIEnv *env;
    int attached = 0;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
            error_log("Failed to get the environment or attach thread");
            return;
        }
        attached = 1;
    }

    if(gService != NULL) {
        jclass cls = (*env)->GetObjectClass(env, gService);
        jmethodID discoveryCbID = (*env)->GetMethodID(env, cls, "discoveryCb", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V");
        if(discoveryCbID != NULL) {
            jstring deviceJava = (*env)->NewStringUTF(env, szDeviceId);
            jstring deviceNameJava = (*env)->NewStringUTF(env, ickP2pGetDeviceName(ictx,szDeviceId));
            jstring deviceLocationJava = (*env)->NewStringUTF(env, ickP2pGetDeviceLocation(ictx,szDeviceId));
            jint changeJava = change;
            jint typeJava = type;
            (*env)->CallVoidMethod(env, gService, discoveryCbID, deviceJava, deviceNameJava,deviceLocationJava,typeJava, changeJava);
        }else {
            error_log("GetMethodID failed");
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
        error_log("Failed to get the environment or attach thread");
        return -1;
    }
#ifdef DEBUG
    debug_log("ickP2pSetLogLevel(7)\n");
    ickP2pSetLogging(7,stderr,100);
#elif ICK_DEBUG
    ickP2pSetLogging(6,NULL,100);
#endif

    lws_set_log_level(255,NULL);
#ifdef __ANDROID_API__
    ickP2pSetLogFacility(&log);
#ifdef DEBUG
    freopen("/sdcard/ickStreamPlayer.log", "w", stderr);
#endif
#endif

    return JNI_VERSION_1_4;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pCreate(JNIEnv * env, jobject service, jstring deviceNameJava, jstring deviceIdJava, jstring dataFolderJava, jint lifetime, jint port, jint type)
{
    gService = (*env)->NewGlobalRef(env, service);
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szDeviceName = NULL;
    if(deviceNameJava != NULL) {
        szDeviceName = (*env)->GetStringUTFChars(env, deviceNameJava, NULL);
    }
    const char * szDataFolder = NULL;
    if(dataFolderJava != NULL) {
        szDataFolder = (*env)->GetStringUTFChars(env, dataFolderJava, NULL);
    }

    if(myDeviceId != NULL) {
        free(myDeviceId);
    }
    myDeviceId = malloc(strlen(szDeviceId)+1);
    strcpy(myDeviceId,szDeviceId);

    ickErrcode_t error;

    debug_log("ickP2pCreate(%s,%s,%s,%d,%d,%d,%p)\n",szDeviceName, szDeviceId,szDataFolder,(int)lifetime,(int)port,(int)type,&error);
    context = ickP2pCreate(szDeviceName, szDeviceId,szDataFolder,lifetime,port,type,&error);
    debug_log("context = %p\n",context);

    debug_log("ickP2pRegisterMessageCallback(%p,%p)\n",context,&messageCb);

    ickP2pRegisterMessageCallback(context,&messageCb);

    debug_log("ickP2pRegisterDiscoveryCallback(%p,%p)\n",context,&discoveryCb);

    ickP2pRegisterDiscoveryCallback(context,&discoveryCb);

#ifdef ICK_DEBUG
    debug_log("ickP2pSetHttpDebugging(%p,1)\n",context,1);
    ickP2pSetHttpDebugging(context,1);
#endif

    (*env)->ReleaseStringUTFChars(env, deviceIdJava, szDeviceId);
    if(szDeviceName != NULL) {
    	(*env)->ReleaseStringUTFChars(env, deviceNameJava, szDeviceName);
    }
    if(szDataFolder != NULL) {
    	(*env)->ReleaseStringUTFChars(env, dataFolderJava, szDataFolder);
    }
    return (jint)error;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pAddInterface(JNIEnv * env, jobject service, jstring interfaceJava, jstring hostnameJava)
{
    gService = (*env)->NewGlobalRef(env, service);

    const char * szInterface = (*env)->GetStringUTFChars(env, interfaceJava, NULL);
    const char * szHostname = NULL;
    if(hostnameJava != NULL) {
        szHostname = (*env)->GetStringUTFChars(env, hostnameJava, NULL);
    }

    debug_log("ickP2pAddInterface(%p,%s,%s)\n",context,szInterface,szHostname);
    ickErrcode_t error = ickP2pAddInterface(context, szInterface, szHostname);

    (*env)->ReleaseStringUTFChars(env, interfaceJava, szInterface);
    if(szHostname != NULL) {
    	(*env)->ReleaseStringUTFChars(env, hostnameJava, szHostname);
    }
    return (jint)error;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pResume(JNIEnv * env, jobject service)
{
    debug_log("ickP2pResume(%p)\n",context);

    ickErrcode_t error = ickP2pResume(context);
    return (jint)error;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pSuspend(JNIEnv * env, jobject service)
{
    debug_log("ickP2pSuspend(%p)\n",context);

    ickErrcode_t error = ickP2pSuspend(context);
    return (jint)error;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pEnd(JNIEnv * env, jobject service)
{
    debug_log("ickP2pEnd(%p)\n",context);

    ickErrcode_t error = ickP2pEnd(context,NULL);
    if(error == ICKERR_SUCCESS) {
        (*env)->DeleteGlobalRef(env, gService);
        gService = NULL;
    }
    return (jint)error;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pSendMsg(JNIEnv * env, jobject this, jstring targetDeviceIdJava, jint targetService, jint sourceService, jbyteArray messageJava)
{
    const char * szTargetDeviceId = NULL;
    if (targetDeviceIdJava != NULL) {
        szTargetDeviceId = (*env)->GetStringUTFChars(env, targetDeviceIdJava, NULL);
    }

    jbyte* byteMessage = (*env)->GetByteArrayElements(env, messageJava, NULL);
    jsize messageLength = (*env)->GetArrayLength(env, messageJava);

    ickErrcode_t error;

    debug_log("ickP2pSendMsg(%p,%s,%d,%d,%s,%d)\n",context,szTargetDeviceId,(int)targetService,(int)sourceService,byteMessage,messageLength);
    error = ickP2pSendMsg(context, szTargetDeviceId, targetService, sourceService, byteMessage, messageLength);
    if(error != ICKERR_SUCCESS) {
        error_log("Failed to send message");
    }

    (*env)->ReleaseByteArrayElements(env, messageJava, byteMessage, JNI_ABORT);
    if (szTargetDeviceId != NULL) {
        (*env)->ReleaseStringUTFChars(env, targetDeviceIdJava, szTargetDeviceId);
    }
    return (jint)error;
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
