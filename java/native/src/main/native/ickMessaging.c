//
//  ickMessaging.c
//  ickStreamP2P
//
// Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
// All rights reserved.
//

#include <stdio.h>
#include <pthread.h>
#include <arpa/inet.h>
#include "ickDiscovery.h"
#include "ickMessaging.h"

pthread_t server_thread_handle = NULL;
int server_socket = 0;
char * myUUID = NULL;

struct _client_connection {
    int socket;
    char addr[40];
};
struct _ickMessageCallbacks {
    struct _ickMessageCallbacks * next;
    ickDevice_message_callback_t callback;
};
static struct _ickMessageCallbacks * _ick_MessageCallbacks = NULL;

struct _ickMessagingDevices {
    struct _ickMessagingDevice * next;
    char * UUID;
    char * addr;
    int socket;
    int read_socket;
    int active;
};
static struct _ickMessagingDevices * _ick_MessagingDevices = NULL;

struct _ickMessagingDevices * _ickMessagingInitConnection(const char * UUID, char * remote_ip);

struct _ickMessagingDevices * findDevice(const char * UUID) {
    struct _ickMessagingDevices * deviceTemp = _ick_MessagingDevices;

    while (deviceTemp) {
        if(strcmp(UUID, deviceTemp->UUID) == 0) {
            return deviceTemp;
        }
        deviceTemp = deviceTemp->next;
    }
    return NULL;
}

int ickDeviceSendMsg(const char * UUID, const void * message, const size_t message_size) {
    struct _ickMessagingDevices * deviceTemp = findDevice(UUID);

    if(deviceTemp != NULL) {
        if(send(deviceTemp->socket,message,message_size,0) == message_size) {
            return 0;
        }
        fprintf(stderr, "Failed to write message to %s (write failure)\n",UUID);
        return -1;
    }
    fprintf(stderr, "Failed to write message to %s (no connection)\n",UUID);
    return -1;
}

char ** ickDeviceList(enum ickDevice_servicetype type) {
	return NULL;
}

int ickDeviceRegisterMessageCallback(ickDevice_message_callback_t callback) {
    struct _ickMessageCallbacks * cbTemp = _ick_MessageCallbacks;

    while (cbTemp)
        if (cbTemp == callback)
            return -1;

    cbTemp = malloc(sizeof(struct _ickMessageCallbacks));
    cbTemp->next = _ick_MessageCallbacks;
    cbTemp->callback = callback;
    _ick_MessageCallbacks = cbTemp;
    return 0;
}

static int _ick_execute_MessageCallback (const char * UUID, const void * message, const size_t message_size) {
    struct _ickMessageCallbacks * cbTemp = _ick_MessageCallbacks;

    while (cbTemp) {
        cbTemp->callback(UUID, message, message_size);
        cbTemp = cbTemp->next;
    }
    return 0;
}

static void* client_thread(void *arg) {
    struct _client_connection * client_connection = (struct client_connection *)arg;

    char UUID[100];
    if(read(client_connection->socket, UUID, 100) > 0) {
        printf("Established read channel to %s\n",UUID);

        // Get or initiate a write connection of it doesn't already exist
        struct _ickMessagingDevices * deviceTemp = _ickMessagingInitConnection(UUID,client_connection->addr);

        if(deviceTemp != NULL) {
            deviceTemp->active = 1;
            deviceTemp->read_socket = client_connection->socket;
        }
        printf("Esablished two-way communication with %s\n",UUID);

        // Start listening for incoming messages
        char buffer[100000];
        int nread = 0;
        while( (nread = read(client_connection->socket, buffer, 100000)) > 0) {
            buffer[nread] = '\0';
            _ick_execute_MessageCallback(UUID, buffer,strlen(buffer));
        }
        printf("Lost contact with %s\n",UUID);

        // Closing write connection and cleaning up
        struct _ickMessagingDevices * previous = NULL;
        deviceTemp = _ick_MessagingDevices;
        while (deviceTemp) {
            if(strcmp(UUID, deviceTemp->UUID) == 0) {
                close(deviceTemp->socket);
                if(previous != NULL) {
                    previous->next = deviceTemp->next;
                }else {
                    _ick_MessagingDevices = deviceTemp->next;
                }
                free(deviceTemp->addr);
                free(deviceTemp->UUID);
                free(deviceTemp);
                break;
            }
            previous = deviceTemp;
            deviceTemp = deviceTemp->next;
        }
    }else {
        fprintf(stderr, "Unable to get identity for device\n");
    }
    close(client_connection->socket);
    free(client_connection);
}

static void* server_thread(void *arg)
{
    server_socket = (int)arg;
    struct sockaddr_in addr;
    socklen_t addr_len = sizeof(addr);

    listen(server_socket,5);
    int socket;
    while((socket = accept(server_socket,
                     (struct sockaddr *) &addr,
                     &addr_len))>=0) {
        struct _client_connection * client_connection = malloc(sizeof(struct _client_connection));
        client_connection->socket = socket;
        strcpy(client_connection->addr,inet_ntoa(addr.sin_addr));
        pthread_t handle = NULL;
        pthread_create(&handle, NULL, client_thread, client_connection);
    }
    close(server_socket);
}


int ickEndMessaging(int wait) {
    if(server_socket!=0) {
        close(server_socket);
    }
    if(wait) {
        pthread_join(server_thread_handle, NULL);
    }
    while (_ick_MessagingDevices) {
        close(_ick_MessagingDevices->socket);
        struct _ickMessagingDevices * temp = _ick_MessagingDevices;
        _ick_MessagingDevices = _ick_MessagingDevices->next;
        free(temp);
    }
}

int ickInitMessaging(const char * UUID, const char * interface) {
    myUUID = malloc(strlen(UUID)+1);
    strcpy(myUUID,UUID);
    int sock = socket(AF_INET,SOCK_STREAM,0);
    if (sock < 0) {
        fprintf(stderr, "Unable to create server socket");
        return ICKDISCOVERY_SOCKET_ERROR;
    }

    int opt = 1;
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(int));
    setsockopt(sock, SOL_SOCKET, SO_KEEPALIVE, &opt, sizeof(int));

    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(20530);
    if (bind(sock, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
        fprintf(stderr, "Unable to bind to server socket");
        return ICKDISCOVERY_SOCKET_ERROR;
    }
    pthread_create(&server_thread_handle, NULL, server_thread, sock);
    return ICKDISCOVERY_SUCCESS;
}

int ickMessagingInitConnection(const char * UUID) {
    if(_ickMessagingInitConnection(UUID,NULL) != NULL) {
        return ICKDISCOVERY_SUCCESS;
    }else {
        return ICKDISCOVERY_SOCKET_ERROR;
    }
}

struct _ickMessagingDevices * _ickMessagingInitConnection(const char * UUID, char * remote_ip) {
    struct _ickMessagingDevices * deviceTemp = findDevice(UUID);
    if(deviceTemp != NULL) {
        return deviceTemp;
    }
    if(remote_ip == NULL) {
        const char * url = ickDeviceURL(UUID);
        if(url == NULL) {
            return NULL;
        }
        remote_ip = url;
        if(strncmp(url,"http://",7) == 0) {
            remote_ip = url + 7;
        }
    }
    struct sockaddr_in addr;
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = inet_addr(remote_ip);
    addr.sin_port = htons(20530);
    int sock = socket(AF_INET,SOCK_STREAM,0);
    int opt = 1;
    setsockopt(sock, SOL_SOCKET, SO_KEEPALIVE, &opt, sizeof(int));

    if(connect(sock, &addr, sizeof(addr)) != -1) {
        if(send(sock, myUUID, strlen(myUUID)+1,0) != -1) {
            printf("Established write channel to %s\n",UUID);
            deviceTemp = malloc(sizeof(struct _ickMessagingDevices));
            deviceTemp->next = _ick_MessagingDevices;
            deviceTemp->UUID = malloc(strlen(UUID) + 1);
            strcpy(deviceTemp->UUID, UUID);
            deviceTemp->addr = malloc(strlen(remote_ip) + 1);
            strcpy(deviceTemp->addr,remote_ip);
            deviceTemp->socket = sock;
            deviceTemp->read_socket = 0;
            deviceTemp->active = 0;
            _ick_MessagingDevices = deviceTemp;

            return deviceTemp;
        }
    }
    close(sock);
    return NULL;
}
