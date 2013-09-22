/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickp2p;

public class IckP2pException extends Exception {
    private int errorCode;

    public static final int ICKERR_GENERIC = 1;
    public static final int ICKERR_NOTIMPLEMENTED = 2;
    public static final int ICKERR_INVALID = 3;
    public static final int ICKERR_UNINITIALIZED = 4;
    public static final int ICKERR_INITIALIZED = 5;
    public static final int ICKERR_WRONGSTATE = 6;
    public static final int ICKERR_NOMEMBER = 7;
    public static final int ICKERR_NOMEM = 8;
    public static final int ICKERR_NOTHREAD = 9;
    public static final int ICKERR_NOINTERFACE = 10;
    public static final int ICKERR_NOSOCKET = 11;
    public static final int ICKERR_NODEVICE = 12;
    public static final int ICKERR_BADURI = 13;
    public static final int ICKERR_NOTCONNECTED = 14;
    public static final int ICKERR_ISCONNECTED = 15;
    public static final int ICKERR_LWSERR = 16;

    public IckP2pException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return "errorCode="+errorCode;
    }
}
