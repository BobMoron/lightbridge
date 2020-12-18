package com.gratteburnes.lightbridge.core.model.error;

import com.gratteburnes.magichome.model.IDevice;

public class DeviceConflictException extends Exception{
    public DeviceConflictException(IDevice device, String field) {
        super(String.format("'%s' already exists, field: '%s'", device, field));
    }
}
