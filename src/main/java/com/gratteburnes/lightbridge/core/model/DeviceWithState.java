package com.gratteburnes.lightbridge.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gratteburnes.magichome.model.*;

import javax.validation.constraints.NotNull;

public class DeviceWithState implements IDevice, IDeviceState {
    @NotNull
    private Device device;

    private DeviceState deviceState;

    public DeviceWithState(@NotNull IDevice idevice) {
        this.device = new Device(idevice);
    }

    public Device getDevice() {
        return device;
    }

    public DeviceWithState setDevice(Device device) {
        this.device = device;
        return this;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public DeviceWithState setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
        return this;
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


    @Override
    @JsonIgnore
    public DeviceOptions getOptions() {
        return deviceState.getOptions();
    }

    @Override
    @JsonIgnore
    public int getType() {
        return deviceState.getType();
    }

    @Override
    @JsonIgnore
    public boolean isOn() {
        return deviceState.isOn();
    }

    @Override
    @JsonIgnore
    public DeviceMode getMode() {
        return deviceState.getMode();
    }

    @Override
    @JsonIgnore
    public DevicePattern getPattern() {
        return deviceState.getPattern();
    }

    @Override
    @JsonIgnore
    public int getIaPatternValue() {
        return deviceState.getIaPatternValue();
    }

    @Override
    @JsonIgnore
    public int getSpeed() {
        return deviceState.getSpeed();
    }

    @Override
    @JsonIgnore
    public int getRed() {
        return deviceState.getRed();
    }

    @Override
    @JsonIgnore
    public int getGreen() {
        return deviceState.getGreen();
    }

    @Override
    @JsonIgnore
    public int getBlue() {
        return deviceState.getBlue();
    }

    @Override
    @JsonIgnore
    public int getWarmWhite() {
        return deviceState.getWarmWhite();
    }

    @Override
    @JsonIgnore
    public int getCoolWhite() {
        return deviceState.getCoolWhite();
    }
}
