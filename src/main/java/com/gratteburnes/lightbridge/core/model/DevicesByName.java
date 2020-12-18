package com.gratteburnes.lightbridge.core.model;

import com.gratteburnes.magichome.model.IDevice;
import lombok.Data;

import java.util.Map;

@Data
public class DevicesByName {
    private long timestamp;
    private Map<String, IDevice> devices;

    public DevicesByName(Map<String, IDevice> devices) {
        timestamp = System.currentTimeMillis();
        this.devices = devices;
    }
}
