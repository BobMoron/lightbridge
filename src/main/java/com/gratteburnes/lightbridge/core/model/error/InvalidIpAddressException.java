package com.gratteburnes.lightbridge.core.model.error;

public class InvalidIpAddressException extends Exception {
    public InvalidIpAddressException(String address) {
        super(String.format("'%s' is not a valid IPV4 address", address));
    }
}
