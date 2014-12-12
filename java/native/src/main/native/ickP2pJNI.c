/**********************************************************************\

Header File     : ickP2pJNI.c

Description     : Java JNI wrapper for ickStream P2P module

Comments        : See http://wiki.ickstream.com/index.php/API/ickP2P_Protocol
                  for detailed description.

Date            : 22.09.2013

Updates         : -

Author          : EI

Remarks         : -

*************************************************************************
 * Copyright (c) 2013, ickStream GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of ickStream nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <syslog.h>
#include <pthread.h>
#ifdef __ANDROID_API__
#include <android/log.h>
#endif
#include "ickP2p.h"
#include "libwebsockets.h"

void ickp2p_android_log( const char *file, int line, int prio, const char * format, ... );
#define DEBUG_TAG "ickP2pJNI"

#ifdef ICK_DEBUG
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

struct _ickP2pJNIContext;
struct _ickP2pJNIContext {
    ickP2pContext_t* context;
    jobject service;
    struct _ickP2pJNIContext* next;
};

struct _ickP2pJNIContext *contexts = NULL;
pthread_mutex_t contextMutex;

jobject getServiceForContext(ickP2pContext_t* context) {
  jobject service = NULL;
  pthread_mutex_lock( &contextMutex );
  if(contexts == NULL) {
      return NULL;
  }
  struct _ickP2pJNIContext* next = contexts;
  while(next != NULL) {
      if(next->context == context) {
          service = next->service;
          break;
      }
      next = next->next;
  }
  pthread_mutex_unlock( &contextMutex );
  return service;
}

ickP2pContext_t* getContextForService(JNIEnv * env, jobject service) {
    ickP2pContext_t* context = NULL;
    pthread_mutex_lock( &contextMutex );

    if(contexts != NULL) {
        struct _ickP2pJNIContext* next = contexts;
        while(next != NULL) {
            if((*env)->IsSameObject(env,next->service,service)) {
                context = next->context;
                break;
            }
            next = next->next;
        }
    }

    pthread_mutex_unlock( &contextMutex );
    return context;
}

void addServiceForContext(ickP2pContext_t* context, jobject service) {

    struct _ickP2pJNIContext* entry = malloc(sizeof(struct _ickP2pJNIContext) );
    entry->context = context;
    entry->service = service;
    entry->next=NULL;

    pthread_mutex_lock( &contextMutex );

    if(contexts == NULL) {
        contexts = entry;
    }else {
        struct _ickP2pJNIContext* next = contexts;
        while(next->next != NULL) {
            next = next->next;
        }
        next->next = entry;
    }

    pthread_mutex_unlock( &contextMutex );
}

void removeServiceForContext(ickP2pContext_t* context) {
    pthread_mutex_lock( &contextMutex );

    if(contexts != NULL) {
        if(contexts->next == NULL) {
            if(contexts->context==context) {
                free(contexts);
                contexts = NULL;
            }
        }else {
            struct _ickP2pJNIContext* next = contexts;
            while(next->next != NULL) {
                if(next->next->context==context) {
                    break;
                }
                next = next->next;
            }
            if(next->next->context == context) {
                free(next->next);
                next->next = NULL;
            }
        }
    }

    pthread_mutex_unlock( &contextMutex );
}

void messageCb(ickP2pContext_t *ictx, const char *szSourceDeviceId, ickP2pServicetype_t sourceService, ickP2pServicetype_t targetService, const char* message, size_t messageLength, ickP2pMessageFlag_t mFlags )
{
    debug_log("messageCb(%p,%s,%s,%d,%d,%d)\n",ictx,szSourceDeviceId,message,(int)messageLength,sourceService,targetService);

    int attached = 0;
    JNIEnv *env;
    if ((*gJavaVM)->GetEnv(gJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        if((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL) < 0) {
            error_log("Failed to get the environment or attach thread");

            return;
        }
        attached = 1;
    }
    jobject service = getServiceForContext(ictx);
    if(service != NULL) {
        jclass cls = (*env)->GetObjectClass(env, service);
        jmethodID messageCbID = (*env)->GetMethodID(env, cls, "messageCb", "(Ljava/lang/String;II[B)V");
        if(messageCbID != NULL) {
            jbyteArray messageJava = (*env)->NewByteArray(env, messageLength);
            (*env)->SetByteArrayRegion(env, messageJava, 0, messageLength, message);
            jstring sourceDeviceJava = (*env)->NewStringUTF(env, szSourceDeviceId);
            jint javaTargetService = targetService;
            jint javaSourceService = sourceService;
            (*env)->CallVoidMethod(env, service, messageCbID, sourceDeviceJava, javaSourceService,javaTargetService, messageJava);
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

    jobject service = getServiceForContext(ictx);
    if(service != NULL) {
        jclass cls = (*env)->GetObjectClass(env, service);
        jmethodID discoveryCbID = (*env)->GetMethodID(env, cls, "discoveryCb", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)V");
        if(discoveryCbID != NULL) {
            jstring deviceJava = (*env)->NewStringUTF(env, szDeviceId);
            jstring deviceNameJava = (*env)->NewStringUTF(env, ickP2pGetDeviceName(ictx,szDeviceId));
            jstring deviceLocationJava = (*env)->NewStringUTF(env, ickP2pGetDeviceLocation(ictx,szDeviceId));
            jint changeJava = change;
            jint typeJava = type;
            (*env)->CallVoidMethod(env, service, discoveryCbID, deviceJava, deviceNameJava,deviceLocationJava,typeJava, changeJava);
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
    pthread_mutex_init( &contextMutex, NULL );
#ifdef DEBUG
    debug_log("ickP2pSetLogLevel(7)\n");
    ickP2pSetLogging(7,stderr,100);
#elif ICK_DEBUG
    ickP2pSetLogging(6,NULL,100);
#endif

    lws_set_log_level(255,NULL);
#ifdef __ANDROID_API__
    ickP2pSetLogFacility(&ickp2p_android_log);
#ifdef DEBUG
    freopen("/sdcard/ickStreamPlayer.log", "w", stderr);
#endif
#endif

    return JNI_VERSION_1_4;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pCreate(JNIEnv * env, jobject service, jstring deviceNameJava, jstring deviceIdJava, jstring dataFolderJava, jint lifetime, jint port, jint type)
{
    const char * szDeviceId = (*env)->GetStringUTFChars(env, deviceIdJava, NULL);
    const char * szDeviceName = NULL;
    if(deviceNameJava != NULL) {
        szDeviceName = (*env)->GetStringUTFChars(env, deviceNameJava, NULL);
    }
    const char * szDataFolder = NULL;
    if(dataFolderJava != NULL) {
        szDataFolder = (*env)->GetStringUTFChars(env, dataFolderJava, NULL);
    }

    ickErrcode_t error;

    debug_log("ickP2pCreate(%s,%s,%s,%d,%d,%d,%p)\n",szDeviceName, szDeviceId,szDataFolder,(int)lifetime,(int)port,(int)type,&error);
    ickP2pContext_t* context = ickP2pCreate(szDeviceName, szDeviceId,szDataFolder,lifetime,port,type,&error);
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
    addServiceForContext(context,(*env)->NewGlobalRef(env, service));
    return (jint)error;
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pAddInterface(JNIEnv * env, jobject service, jstring interfaceJava, jstring hostnameJava)
{
    const char * szInterface = (*env)->GetStringUTFChars(env, interfaceJava, NULL);
    const char * szHostname = NULL;
    if(hostnameJava != NULL) {
        szHostname = (*env)->GetStringUTFChars(env, hostnameJava, NULL);
    }
    ickP2pContext_t* context = getContextForService(env, service);
    debug_log("ickP2pAddInterface(%p,%s,%s)\n",context,szInterface,szHostname);

    if(context != NULL) {
        ickErrcode_t error = ickP2pAddInterface(context, szInterface, szHostname);

        (*env)->ReleaseStringUTFChars(env, interfaceJava, szInterface);
        if(szHostname != NULL) {
            (*env)->ReleaseStringUTFChars(env, hostnameJava, szHostname);
        }
        return (jint)error;
    }else {
        return ICKERR_INVALID;
    }
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pResume(JNIEnv * env, jobject service)
{
    ickP2pContext_t* context = getContextForService(env, service);
    debug_log("ickP2pResume(%p)\n",context);

    if(context != NULL) {
        ickErrcode_t error = ickP2pResume(context);
        return (jint)error;
    }else {
        return ICKERR_INVALID;
    }
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pSuspend(JNIEnv * env, jobject service)
{
    ickP2pContext_t* context = getContextForService(env, service);
    debug_log("ickP2pSuspend(%p)\n",context);

    if(context != NULL) {
        ickErrcode_t error = ickP2pSuspend(context);
        return (jint)error;
    }else {
        return ICKERR_INVALID;
    }
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pUpnpLoopback(JNIEnv * env, jobject service, jint enable )
{
    ickP2pContext_t* context = getContextForService(env, service);
    debug_log("ickP2pUpnpLoopback(%p,%d)\n",context,(int)enable);

    if(context != NULL) {
        ickErrcode_t error = ickP2pUpnpLoopback(context, enable);
        return (jint)error;
    }else {
        return ICKERR_INVALID;
    }
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pEnd(JNIEnv * env, jobject service)
{
    ickP2pContext_t* context = getContextForService(env, service);
    debug_log("ickP2pEnd(%p)\n",context);

    if(context != NULL) {
        ickErrcode_t error = ickP2pEnd(context,NULL);
        if(error == ICKERR_SUCCESS) {
            jobject service = getServiceForContext(context);
            removeServiceForContext(context);
            (*env)->DeleteGlobalRef(env, service);
        }
        return (jint)error;
    }else {
        return ICKERR_INVALID;
    }
}

jint Java_com_ickstream_common_ickp2p_IckP2pJNI_ickP2pSendMsg(JNIEnv * env, jobject service, jstring targetDeviceIdJava, jint targetService, jint sourceService, jbyteArray messageJava)
{
    const char * szTargetDeviceId = NULL;
    if (targetDeviceIdJava != NULL) {
        szTargetDeviceId = (*env)->GetStringUTFChars(env, targetDeviceIdJava, NULL);
    }

    jbyte* byteMessage = (*env)->GetByteArrayElements(env, messageJava, NULL);
    jsize messageLength = (*env)->GetArrayLength(env, messageJava);

    ickErrcode_t error;

    ickP2pContext_t* context = getContextForService(env, service);
    if(context != NULL) {

    	// 0-terminate byteMessage for logging
    	char* stringMessage = malloc(messageLength + 1);
    	memcpy(stringMessage, byteMessage, messageLength);
    	stringMessage[messageLength] = '\0';


        debug_log("ickP2pSendMsg(%p,%s,%d,%d,%s,%d)\n",context,szTargetDeviceId,(int)targetService,(int)sourceService,stringMessage,messageLength);
        free(stringMessage);

        error = ickP2pSendMsg(context, szTargetDeviceId, targetService, sourceService, byteMessage, messageLength);
    }else {
        error = ICKERR_INVALID;
    }

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
void ickp2p_android_log( const char *file, int line, int prio, const char * format, ... )
{
    va_list argptr;
    va_start(argptr,format);
    switch(prio) {
        case LOG_EMERG:
        case LOG_ALERT:
            __android_log_vprint(ANDROID_LOG_FATAL,"ickP2p",format,argptr);
            break;
        case LOG_CRIT:
        case LOG_ERR:
            __android_log_vprint(ANDROID_LOG_ERROR,"ickP2p",format,argptr);
            break;
        case LOG_WARNING:
            __android_log_vprint(ANDROID_LOG_WARN,"ickP2p",format,argptr);
            break;
        case LOG_NOTICE:
        case LOG_INFO:
            __android_log_vprint(ANDROID_LOG_INFO,"ickP2p",format,argptr);
            break;
        case LOG_DEBUG:
        default:
            __android_log_vprint(ANDROID_LOG_DEBUG,"ickP2p",format,argptr);
            break;
    }
    va_end(argptr);
}
#endif
