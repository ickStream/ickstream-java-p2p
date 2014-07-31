/*
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
        return "errorCode=" + errorCode;
    }
}
