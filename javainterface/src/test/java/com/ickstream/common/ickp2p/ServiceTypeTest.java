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

import junit.framework.Assert;
import org.testng.annotations.Test;

public class ServiceTypeTest {
    @Test
    public void testValueOf() {
        Assert.assertTrue(ServiceType.GENERIC == ServiceType.valueOf(0));
        Assert.assertTrue(ServiceType.PLAYER == ServiceType.valueOf(1));
        Assert.assertTrue(ServiceType.CONTROLLER == ServiceType.valueOf(2));
        Assert.assertTrue(ServiceType.SERVICE == ServiceType.valueOf(4));
        Assert.assertTrue(ServiceType.DEBUG == ServiceType.valueOf(8));
        Assert.assertTrue(ServiceType.ANY == ServiceType.valueOf(15));
        Assert.assertEquals(ServiceType.valueOf(3), ServiceType.valueOf(3));
        Assert.assertEquals(ServiceType.valueOf(42), ServiceType.valueOf(42));
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(ServiceType.GENERIC, ServiceType.valueOf(0));
        Assert.assertEquals(ServiceType.PLAYER, ServiceType.valueOf(1));
        Assert.assertEquals(ServiceType.CONTROLLER, ServiceType.valueOf(2));
        Assert.assertEquals(ServiceType.SERVICE, ServiceType.valueOf(4));
        Assert.assertEquals(ServiceType.DEBUG, ServiceType.valueOf(8));
        Assert.assertEquals(ServiceType.ANY, ServiceType.valueOf(15));
        Assert.assertEquals(ServiceType.valueOf(3), ServiceType.valueOf(3));
        Assert.assertEquals(ServiceType.valueOf(42), ServiceType.valueOf(42));
    }

    @Test
    public void testIsType() {
        Assert.assertTrue(ServiceType.valueOf(0).isType(ServiceType.GENERIC));
        Assert.assertTrue(ServiceType.valueOf(1).isType(ServiceType.GENERIC));

        Assert.assertTrue(ServiceType.valueOf(1).isType(ServiceType.PLAYER));
        Assert.assertFalse(ServiceType.valueOf(1).isType(ServiceType.CONTROLLER));
        Assert.assertFalse(ServiceType.valueOf(1).isType(ServiceType.SERVICE));

        Assert.assertFalse(ServiceType.valueOf(2).isType(ServiceType.PLAYER));
        Assert.assertTrue(ServiceType.valueOf(2).isType(ServiceType.CONTROLLER));
        Assert.assertFalse(ServiceType.valueOf(2).isType(ServiceType.SERVICE));

        Assert.assertFalse(ServiceType.valueOf(4).isType(ServiceType.PLAYER));
        Assert.assertFalse(ServiceType.valueOf(4).isType(ServiceType.CONTROLLER));
        Assert.assertTrue(ServiceType.valueOf(4).isType(ServiceType.SERVICE));

        Assert.assertTrue(ServiceType.valueOf(3).isType(ServiceType.PLAYER));
        Assert.assertTrue(ServiceType.valueOf(3).isType(ServiceType.CONTROLLER));
        Assert.assertFalse(ServiceType.valueOf(3).isType(ServiceType.SERVICE));

        Assert.assertTrue(ServiceType.valueOf(5).isType(ServiceType.PLAYER));
        Assert.assertTrue(ServiceType.valueOf(5).isType(ServiceType.SERVICE));
        Assert.assertFalse(ServiceType.valueOf(5).isType(ServiceType.CONTROLLER));

        Assert.assertTrue(ServiceType.valueOf(6).isType(ServiceType.CONTROLLER));
        Assert.assertTrue(ServiceType.valueOf(6).isType(ServiceType.SERVICE));
        Assert.assertFalse(ServiceType.valueOf(6).isType(ServiceType.PLAYER));

        Assert.assertTrue(ServiceType.valueOf(7).isType(ServiceType.CONTROLLER));
        Assert.assertTrue(ServiceType.valueOf(7).isType(ServiceType.SERVICE));
        Assert.assertTrue(ServiceType.valueOf(7).isType(ServiceType.PLAYER));
        Assert.assertFalse(ServiceType.valueOf(7).isType(ServiceType.DEBUG));

        Assert.assertTrue(ServiceType.valueOf(15).isType(ServiceType.CONTROLLER));
        Assert.assertTrue(ServiceType.valueOf(15).isType(ServiceType.SERVICE));
        Assert.assertTrue(ServiceType.valueOf(15).isType(ServiceType.PLAYER));
        Assert.assertTrue(ServiceType.valueOf(15).isType(ServiceType.DEBUG));
        Assert.assertTrue(ServiceType.valueOf(15).isType(ServiceType.ANY));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("PLAYER", ServiceType.valueOf(1).toString());

        Assert.assertEquals("CONTROLLER", ServiceType.valueOf(2).toString());

        Assert.assertEquals("SERVICE", ServiceType.valueOf(4).toString());

        Assert.assertEquals("PLAYER,CONTROLLER", ServiceType.valueOf(3).toString());

        Assert.assertEquals("PLAYER,SERVICE", ServiceType.valueOf(5).toString());

        Assert.assertEquals("CONTROLLER,SERVICE", ServiceType.valueOf(6).toString());

        Assert.assertEquals("ANY", ServiceType.valueOf(15).toString());
    }

}
