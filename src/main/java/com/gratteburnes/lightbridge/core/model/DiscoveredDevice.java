package com.gratteburnes.lightbridge.core.model;

import com.gratteburnes.magichome.model.Device;
import com.gratteburnes.magichome.model.IDevice;

public class DiscoveredDevice extends DeviceWithMessage {
    private boolean added;

    public DiscoveredDevice(IDevice device, boolean added) {
        super(device);
        setAdded(added);
    }

    public DiscoveredDevice(IDevice device, boolean added, String message) {
        super(device, message);
        setAdded(added);
    }

    public boolean isAdded() {
        return added;
    }

    public DiscoveredDevice setAdded(boolean added) {
        this.added = added;
        return this;
    }
}
