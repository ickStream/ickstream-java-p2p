#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include "ickDiscovery.h"

// This is just a stupid simulator
pthread_t gThread;
ickDevice_message_callback_t gCallback = NULL;
ickDiscovery_device_callback_t gDeviceCallback = NULL;
int gAborted = 0;
const char gDeviceId[100];

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

int ickDeviceSendMsg(const char * UUID, const void * message, const size_t message_size)
{
    printf("%s\n",message);
    return 1;
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
    pthread_create(&gThread, NULL, player_thread_main, NULL);
}

void ickEndDiscovery(int wait) {
    gAborted = 1;
}

void ickDiscoveryAddService(enum ickDevice_servicetype type) {

}
