package com.ickstream.common.ickdiscovery;

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
