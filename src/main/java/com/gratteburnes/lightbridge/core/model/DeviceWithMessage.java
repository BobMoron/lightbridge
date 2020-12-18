package com.gratteburnes.lightbridge.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gratteburnes.magichome.model.Device;
import com.gratteburnes.magichome.model.IDevice;

import javax.validation.constraints.NotNull;

public class DeviceWithMessage implements IDevice {
    @NotNull
    private Device device;

    private String message = "";

    public DeviceWithMessage(IDevice source) {
        this.device = new Device(source);
    }

    public DeviceWithMessage(IDevice source, String message) {
        this(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public DeviceWithMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    @JsonIgnore
    public String getIp() {
        return device.getIp();
    }

    @Override
    @JsonIgnore
    public String getMac() {
        return device.getMac();
    }

    @Override
    @JsonIgnore
    public String getName() {
        return device.getName();
    }
}
