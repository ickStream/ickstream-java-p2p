//
//  ickDiscovery.h
//  ickStreamP2P
//
//  Created by Jörg Schwieder on 16.01.12.
//  Copyright (c) 2012 Du!Business GmbH. All rights reserved.
//

#ifndef ickStreamProto_ickDiscovery_h
#define ickStreamProto_ickDiscovery_h

#include <sys/socket.h>

#ifdef __cplusplus
extern "C" {
#endif

    enum ickDiscovery_result {
        ICKDISCOVERY_SUCCESS            = 0,
        ICKDISCOVERY_RUNNING            = 1,
        ICKDISCOVERY_SOCKET_ERROR       = 2,
        ICKDISCOVERY_THREAD_ERROR       = 3,
        ICKDISCOVERY_MEMORY_ERROR       = 4
    };

    enum ICKDISCOVERY_SSDP_TYPES {
        ICKDISCOVERY_TYPE_NOTIFY,
        ICKDISCOVERY_TYPE_SEARCH,
        ICKDISCOVERY_TYPE_RESPONSE,

        ICKDISCOVERY_SSDP_TYPES_COUNT
    };

    enum ickDiscovery_command {
        ICKDISCOVERY_ADD_DEVICE,
        ICKDISCOVERY_REMOVE_DEVICE,
        ICKDISCOVERY_UPDATE_DEVICE
    };

    enum ickDevice_servicetype {
        ICKDEVICE_GENERIC           = 0,
        ICKDEVICE_PLAYER            = 0x1,
        ICKDEVICE_CONTROLLER        = 0x2,
        ICKDEVICE_SERVER_GENERIC    = 0x4
    };

    struct _ick_discovery_struct;
    typedef struct _ick_discovery_struct ickDiscovery_t;

    // Communication setup commands

    // Start the P2P component by initiating the discovery handler. Without it, no connections are being built
    //
    // UUID is a valid UPnP UUID,
    // interface is the IP address or interface of the network interface to be used (e.g. "192.168.0.19" or "en0"). Using an interface ("en0") is recommended right now.
    // No Multi-homing right now.
    //
    // Spawns a communication handling thread and is reentrant but will not do anything on the second or subsequent call (other than returning ICKDISCOVERY_RUNNING
    //
    enum ickDiscovery_result ickInitDiscovery(const char * UUID, const char * interface);

    // Stop the P2P component. "wait" is a synchronicity flag, it makes the function block until the discovery thread has really ended
    // Currently, using "wait" is recommended
    // May only be called from the main thread
    //
    void ickEndDiscovery(int wait);

    //
    // This is a prototype hack for players and controllers only
    // In the long run, we will want to have a full-scale service registry including hooks for the actual service
    // Players and controllers don't need this, though, both are fully being driven through the ickStream connection
    // this currently just ORs a capability into the type enum and sends out an updated capability discovery message
    //
    // Since the capabilities are bound to the discovery object, all Services have to be re-added after an ickEndDiscovery. We should consider whether we want an "ickDiscoverySuspend and ickDiscoveryResume to keep the object alive while shutting down the actual operation.
    //
    void ickDiscoveryAddService(enum ickDevice_servicetype type);

    //
    // Remove a capability. We probably want to be able to correctly shut down a player.
    // Not needed befor ickEndDiscovery since this will announce the shutdown of the whole device.
    //
    void ickDiscoveryRemoveService(enum ickDevice_servicetype type);


    /* These are the main commands to communicate to the P2P Core for player/controller communication.
     Service communication would be somewhat similar but uses different protocols.
     The main difference will probably be that a service has an address/service prefix where a player/controller has a UUID.

     Currently, there is NO protocol handling whatsoever in this, all protocol components have to be embedded in the JSON message(s) being sent, this includes request IDs.

     General Syntax:
     return value:
     0 - success;
     negative values: errors
     TBD: meaningful error values and an error details retrieval system, currently, only -1 is being returned on any error.
     TBD: define a results enum to cpature this.

     char * UUID:       the unique identifier of the device you want to communicate with. For services this would be the service prefix.
     void * message:    the message to be sent in raw, encoded format (that is: a valid, encoded JSON/RPC command.
                        TBD: If we want to allow bundling of several commands in an array, we need to define that on the application layer level; here, the encoded array would be supplied
     size_t size:       the size (in bytes) of the encoded message. Must nox exceed 64KB. TBD: Is this enough? I assume 16 bit size parameters in the protocol, causing this limitation. We probably don't want chunks that are all too big.

     TBD: Define whether to also support synchronous calls that wait for a reply.
     TODO: Currently there is no way to de-register callbacks other than to stop the whole ickDiscovery
     */

    //
    // List available devices
    // Return value is a list of char * UUID values
    // type valie is a value of ORed together types, all of which have to match the type criteria of a device to be returned
    //
    char ** ickDeviceList(enum ickDevice_servicetype type);

    //
    //  get the type of device UUID
    //  TBD: we might want to return a pointer to the whole service definition once we've defined service definitions....
    //
    enum ickDevice_servicetype ickDeviceType(const char * UUID);

    //
    // Callback function type for callback that is being called whenever a device gets added or removed
    //
    typedef void (* ickDiscovery_device_callback_t)(const char * device, enum ickDiscovery_command change, enum ickDevice_servicetype type);

    //
    // register device list callback
    // TBD: We might want to expose the internal registration callback that also gets notifications for UPnP devices but required a whole set of additional type definitions because there's more information required for these than just a UUID.
    //
    int ickDeviceRegisterDeviceCallback(ickDiscovery_device_callback_t callback);


    // send a message to device UUID
    int ickDeviceSendMsg(const char * UUID, const void * message, const size_t message_size);

    //
    // Callback function type for callback that is being called whenever a message comes in
    // NOTE: since ickStreamP2P currently is completely asynchronous, both notifications and replies are being retrievd through this.
    // TBD: we should define whether we want to separate notifications and replies but this requires P2P to inspect the content
    //
    typedef void (* ickDevice_message_callback_t)(const char * UUID, const void * message, const size_t message_size);

    //
    // register message callback
    //
    int ickDeviceRegisterMessageCallback(ickDevice_message_callback_t callback);


#ifdef __cplusplus
}
#endif

#endif
