#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <arpa/inet.h>
#include "ickDiscovery.h"

// This is just a stupid simulator
pthread_t gThread;
pthread_t gBroadcastListenerThread;
pthread_t gBroadcastPosterThread;
ickDevice_message_callback_t gCallback = NULL;
ickDiscovery_device_callback_t gDeviceCallback = NULL;
int gAborted = 0;
#define MAX_CLIENTS 100
const char gDeviceId[MAX_CLIENTS];
int gClientWriteSockets[MAX_CLIENTS];
char * gClientWriteIdentities[MAX_CLIENTS];
int gServerSocket;
struct sockaddr_in gServerAddr;
int gBroadcastListenerSocket;
struct sockaddr_in gBroadcastListenerAddr;
pthread_t gClientThreads[MAX_CLIENTS];
int nextClientThreadNo = 0;
int nextClientNo = 0;


static void* player_thread_main(void *arg)
{
    char player[100];
    sprintf(player,"Player %s",gDeviceId);
    int i=0;
    while(!gAborted) {
        char buffer[100];
        sprintf(buffer,"Are you there ? (%d)",i);
        gCallback(player, buffer,strlen(buffer));
        sleep(1);
        i++;
    }
    printf("Exiting discovery thread\n");
    return NULL;
}

static void* client_thread(void *arg) {
    int socket = (int)arg;

    char player[100];
    if(read(socket, player, 100) > 0) {
        printf("Connected read channel from %s\n",player);

        char buffer[100000];
        int nread = 0;
        puts("Waiting for message data");
        while( (nread = read(socket, buffer, 100000)) > 0) {
            buffer[nread] = '\0';
            gCallback(player, buffer,strlen(buffer));
        }
        printf("Lost contact with %s\n",player);
        int i;
        for(i=0;i<MAX_CLIENTS;i++) {
            if(gClientWriteIdentities[i]!=NULL && strcmp(gClientWriteIdentities[i],player) == 0) {
                char* identity = gClientWriteIdentities[i];
                gClientWriteIdentities[i] = NULL;
                close(gClientWriteSockets[i]);
                gClientWriteSockets[i] = 0;
                free(identity);
                break;
            }
        }
        if(gDeviceCallback!=NULL) {
            gDeviceCallback(player,ICKDISCOVERY_REMOVE_DEVICE,ICKDEVICE_GENERIC);
        }
    }
    close(socket);
}

static void* broadcast_thread(void *arg)
{
    int sock;
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        puts("Failed to create socket");
    }
    struct sockaddr_in broadcastAddr;
    /* Construct the server sockaddr_in structure */
    memset(&broadcastAddr, 0, sizeof(broadcastAddr));       /* Clear struct */
    broadcastAddr.sin_family = AF_INET;                  /* Internet/IP */
    broadcastAddr.sin_addr.s_addr = inet_addr("172.16.0.255");  /* IP address */
    broadcastAddr.sin_port = htons(20531);       /* server port */
    int opt = 1;
    setsockopt(sock, SOL_SOCKET, SO_BROADCAST, &opt, sizeof(int));

    while(!gAborted) {
        puts("Posting broadcast package");
        char buffer[100];
        sprintf(buffer,"ickStream:%s",gDeviceId);
        if (sendto(sock, buffer, strlen(buffer), 0,
                   (struct sockaddr *) &broadcastAddr,
                   sizeof(broadcastAddr)) != strlen(buffer)) {
          puts("Mismatch in number of sent bytes");
        }
        sleep(5);
    }
    close(sock);
    puts("Stop broadcasting");
}

void connectToServer(const char * player, struct sockaddr_in clientAddr) {
    int found = 0;
    int i;
    for(i=0;i<MAX_CLIENTS;i++) {
        if(gClientWriteIdentities[i]!=NULL && strcmp(gClientWriteIdentities[i],player) == 0) {
            found = 1;
            break;
        }
    }
    if(found == 0) {
        gClientWriteIdentities[nextClientNo] = malloc(strlen(player)+1);
        strcpy(gClientWriteIdentities[nextClientNo],player);
        struct sockaddr_in sendAddr;
        sendAddr.sin_family = AF_INET;
        sendAddr.sin_addr.s_addr = clientAddr.sin_addr.s_addr;
        sendAddr.sin_port = htons(20530);
        gClientWriteSockets[nextClientNo] = socket(AF_INET,SOCK_STREAM,0);
        int opt = 1;
        setsockopt(gServerSocket, SOL_SOCKET, SO_KEEPALIVE, &opt, sizeof(int));

        if(connect(gClientWriteSockets[nextClientNo], &sendAddr, sizeof(sendAddr)) == -1) {
            puts("Error when connecting to server");
            close(gClientWriteSockets[nextClientNo]);
        }else {
            if(send(gClientWriteSockets[nextClientNo], gDeviceId, strlen(gDeviceId)+1,0) != -1) {
                printf("Connected write channel to %s\n",player);
                if(gDeviceCallback!=NULL) {
                    gDeviceCallback(gClientWriteIdentities[nextClientNo],ICKDISCOVERY_ADD_DEVICE,ICKDEVICE_GENERIC);
                }
                nextClientNo = nextClientNo + 1;
                return;
            }
            close(gClientWriteSockets[nextClientNo]);
            free(gClientWriteIdentities[nextClientNo]);
            gClientWriteIdentities[nextClientNo] = NULL;
        }
    }
}

static void* broadcast_listener_thread(void *arg)
{
    struct sockaddr_in clientAddr;
    socklen_t clientAddrLength = sizeof(clientAddr);

    char buffer[100];
    int received;

    while ((received = recvfrom(gBroadcastListenerSocket, buffer, 100, 0,
                                           (struct sockaddr *) &clientAddr,
                                           &clientAddrLength)) >= 0) {

        if(gAborted) {
            break;
        }
        if(strncmp(buffer,"ickStream:",10) == 0) {
            char* player = buffer+10;
            if(strcmp(player,gDeviceId)!=0) {
                connectToServer(player,clientAddr);
            }
        }

    }
    close(gBroadcastListenerSocket);
    puts("Stop listening for broadcasting messages");
}

static void* server_thread(void *arg)
{

    struct sockaddr_in clientAddr;
    socklen_t clientAddrLength = sizeof(clientAddr);

    listen(gServerSocket,5);
    puts("Waiting for incomming connections");
    int socket;
    while((socket = accept(gServerSocket,
                     (struct sockaddr *) &clientAddr,
                     &clientAddrLength))>=0) {
        pthread_create(&gClientThreads[nextClientThreadNo], NULL, client_thread, socket);
        nextClientThreadNo = nextClientThreadNo + 1;

    }
    close(gServerSocket);
    puts("Exiting discovery thread");

}

int ickDeviceSendMsg(const char * UUID, const void * message, const size_t message_size)
{
    int i;
    for(i=0;i<MAX_CLIENTS;i++) {
        if(gClientWriteIdentities[i]!=NULL && strcmp(gClientWriteIdentities[i],UUID)==0) {
            if(gClientWriteSockets[i] != 0) {
                if(send(gClientWriteSockets[i],message,message_size,0) == message_size) {
                    return 1;
                }
            }
        }
    }
    return 0;
}

int ickDeviceRegisterMessageCallback(ickDevice_message_callback_t callback) {
    gCallback = callback;
}

int ickDeviceRegisterDeviceCallback(ickDiscovery_device_callback_t callback) {
    gDeviceCallback = callback;
}

void ickDiscoveryRemoveService(enum ickDevice_servicetype type) {
}

char ** ickDeviceList(enum ickDevice_servicetype type) {
    return NULL;
}

enum ickDevice_servicetype ickDeviceType(const char * UUID) {
    return ICKDEVICE_GENERIC;
}

enum ickDiscovery_result ickInitDiscovery(const char * UUID, const char * interface) {
    gAborted = 0;

    strcpy(gDeviceId,UUID);
    int i;
    for(i=0;i<MAX_CLIENTS;i++) {
        gClientWriteIdentities[i] = NULL;
        gClientWriteSockets[i] = 0;
    }

    // Setting up server socket
    gServerSocket = socket(AF_INET,SOCK_STREAM,0);
    if (gServerSocket < 0) {
        puts("Unable to create server socket");
        return ICKDISCOVERY_SOCKET_ERROR;
    }

    int opt = 1;
    setsockopt(gServerSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(int));
    setsockopt(gServerSocket, SOL_SOCKET, SO_KEEPALIVE, &opt, sizeof(int));

    gServerAddr.sin_family = AF_INET;
    gServerAddr.sin_addr.s_addr = INADDR_ANY;
    gServerAddr.sin_port = htons(20530);
    if (bind(gServerSocket, (struct sockaddr *) &gServerAddr, sizeof(gServerAddr)) < 0) {
        puts("Unable to bind to server socket");
        return ICKDISCOVERY_SOCKET_ERROR;
    }

    // Setting up server listener socket
    gBroadcastListenerSocket = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (gBroadcastListenerSocket < 0) {
        puts("Unable to create server socket");
        return ICKDISCOVERY_SOCKET_ERROR;
    }
    gBroadcastListenerAddr.sin_family = AF_INET;
    gBroadcastListenerAddr.sin_addr.s_addr = INADDR_ANY;
    gBroadcastListenerAddr.sin_port = htons(20531);
    if (bind(gBroadcastListenerSocket, (struct sockaddr *) &gBroadcastListenerAddr, sizeof(gBroadcastListenerAddr)) < 0) {
        puts("Unable to bind to broadcast socket");
        return ICKDISCOVERY_SOCKET_ERROR;
    }

    pthread_create(&gThread, NULL, server_thread, NULL);
    pthread_create(&gBroadcastPosterThread, NULL, broadcast_thread, NULL);
    pthread_create(&gBroadcastListenerThread, NULL, broadcast_listener_thread, NULL);
}

void ickEndDiscovery(int wait) {
    gAborted = 1;
    close(gBroadcastListenerSocket);
    close(gServerSocket);
    int i;
    for(i=0;i<MAX_CLIENTS;i++) {
        if(gClientWriteIdentities[i]!=NULL) {
            char* identity = gClientWriteIdentities[i];
            gClientWriteIdentities[i] = NULL;
            if(gClientWriteSockets[i]!=0) {
                close(gClientWriteSockets[i]);
            }
            gClientWriteSockets[i] = 0;
            free(identity);
        }
    }
}

void ickDiscoveryAddService(enum ickDevice_servicetype type) {

}
