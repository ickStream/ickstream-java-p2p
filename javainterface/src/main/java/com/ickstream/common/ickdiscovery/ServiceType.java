package com.ickstream.common.ickdiscovery;

import java.util.Arrays;
import java.util.List;

public class ServiceType {
    static final ServiceType GENERIC = new ServiceType(0,"GENERIC");
    public static final ServiceType PLAYER = new ServiceType(1,"PLAYER");
    public static final ServiceType CONTROLLER = new ServiceType(2,"CONTROLLER");
    public static final ServiceType SERVICE = new ServiceType(4,"SERVICE");

    private static final List<ServiceType> availableTypes = Arrays.asList(
            PLAYER, CONTROLLER, SERVICE
    );

    private int value;
    private String name;
    private ServiceType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int value() {
        return value;
    }

    public boolean isType(ServiceType serviceType) {
        return GENERIC.value == serviceType.value || (this.value & serviceType.value) != 0;
    }

    public static ServiceType valueOf(int value) {
        if(value==0) {
            return GENERIC;
        }
        for (ServiceType type : availableTypes) {
            if(type.value == value) {
                return type;
            }
        }
        return new ServiceType(value,null);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ServiceType && ((ServiceType)o).value ==this.value;
    }

    @Override
    public String toString() {
        if(value!=0) {
            StringBuilder sb = new StringBuilder();
            for (ServiceType serviceType : availableTypes) {
                if(isType(serviceType)) {
                    sb.append(sb.length()>0?",":"").append(serviceType.name);
                }
            }
            if(sb.length()==0) {
                sb.append("UNKNOWN(").append(value).append(")");
            }
            return sb.toString();
        }else {
            return GENERIC.name;
        }
    }
}
