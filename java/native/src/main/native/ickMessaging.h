//
//  ickMessaging.h
//  ickStreamP2P
//
// Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
// All rights reserved.
//

#ifndef ickStreamProto_ickMessaging_h
#define ickStreamProto_ickMessaging_h

#include <sys/socket.h>

#ifdef __cplusplus
extern "C" {
#endif

int ickInitConnection(const char * UUID, const char * remote_ip);
int ickInitMessaging(const char * UUID, const char * interface);
int ickEndMessaging(int wait);

#ifdef __cplusplus
}
#endif

#endif
