package com.gratteburnes.lightbridge.core.model.error;

import com.gratteburnes.magichome.model.error.DeviceException;

public class UnknownDeviceException extends DeviceException {
    public UnknownDeviceException(String name) {
        super(name + " is not registered as a device");
    }
}
