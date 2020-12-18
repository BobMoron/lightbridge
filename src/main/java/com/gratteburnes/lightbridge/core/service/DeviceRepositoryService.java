package com.gratteburnes.lightbridge.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gratteburnes.lightbridge.core.model.DeviceWithMessage;
import com.gratteburnes.lightbridge.core.model.DeviceWithState;
import com.gratteburnes.lightbridge.core.model.error.DeviceConflictException;
import com.gratteburnes.lightbridge.core.model.error.InvalidIpAddressException;
import com.gratteburnes.lightbridge.core.model.error.UnknownDeviceException;
import com.gratteburnes.magichome.model.Device;
import com.gratteburnes.magichome.model.DeviceState;
import com.gratteburnes.magichome.model.IDevice;
import com.gratteburnes.magichome.model.error.DeviceException;
import com.gratteburnes.magichome.model.error.DiscoveryException;
import com.gratteburnes.magichome.model.error.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceRepositoryService {

    private final LightbridgeService lightbridgeService;
    private final ObjectMapper objectMapper;
    private final String deviceFile;

    private Map<String, IDevice> devicesByKey;
    private Map<String, String> keyByMac;
    private Map<String, String> keyByIp;
    private Map<String, String> keyByName;

    public DeviceRepositoryService(ObjectMapper objectMapper, @Value("${lightbridge.persistence.location}") String deviceFile, LightbridgeService lightbridgeService) throws IOException, DeviceConflictException, DiscoveryException {
        this.objectMapper = objectMapper;
        this.deviceFile = deviceFile;
        this.lightbridgeService = lightbridgeService;

        this.keyByIp = new HashMap<>();
        this.keyByName = new HashMap<>();
        this.keyByMac = new HashMap<>();
        if (StringUtils.isBlank(deviceFile)) {
            throw new IllegalArgumentException("Device file cannot be blank");
        }
        initialize();
    }

    public void addDevice(IDevice device) throws DeviceConflictException, IOException {
        validateNewDevice(device);
        String deviceKey = buildDeviceKey(device);
        devicesByKey.put(deviceKey, device);
        keyByIp.put(device.getIp(), deviceKey);
        keyByMac.put(device.getMac(), deviceKey);
        keyByName.put(device.getName(), deviceKey);


        log.info("added device {} -> {}", deviceKey, device);
        persistRepository();
    }

    private void validateNewDevice(IDevice device) throws DeviceConflictException {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        // do not validate name as duplicates are allowed
        if (devicesByKey.containsKey(buildDeviceKey(device))) {
            throw new DeviceConflictException(device, "key");
        }
        if (keyByMac.containsKey(device.getMac())) {
            throw new DeviceConflictException(device, "mac");
        }
        if (keyByIp.containsKey(device.getIp())) {
            throw new DeviceConflictException(device, "ip");
        }
    }

    public void clearDevices() throws IOException {
        devicesByKey.clear();

        keyByIp.clear();
        keyByMac.clear();
        keyByName.clear();

        log.info("cleared devices");
        persistRepository();
    }

    public IDevice removeDevice(String ipOrName) throws IOException, UnknownDeviceException {
        if (StringUtils.isBlank(ipOrName)) {
            throw new IllegalArgumentException("Device name cannot be blank");
        }
        IDevice device = getDeviceFromString(ipOrName);
        if (device == null) {
            throw new UnknownDeviceException(ipOrName);
        } else {
            devicesByKey.remove(buildDeviceKey(device));
            keyByMac.remove(device.getMac());
            keyByIp.remove(device.getIp());
            keyByName.remove(device.getName());
        }

        log.info("removed device {}", ipOrName);
        persistRepository();
        return device;
    }

    public void renameDevice(String ipOrName, String newName) throws IOException, UnknownDeviceException {
        if (StringUtils.isBlank(ipOrName)) {
            throw new IllegalArgumentException("ipOrName name cannot be blank");
        }
        if (StringUtils.isBlank(newName)) {
            throw new IllegalArgumentException("New name cannot be blank");
        }

        IDevice oldDevice = getDeviceFromString(ipOrName);
        if (oldDevice == null) {
            log.info("{} does not map to a device", ipOrName);
            throw new UnknownDeviceException(ipOrName);
        } else {
            String oldName = oldDevice.getName();
            log.info("Renaming {} to {}", oldName, newName);
            devicesByKey.remove(buildDeviceKey(oldDevice));
            keyByIp.remove(oldDevice.getIp());
            keyByMac.remove(oldDevice.getMac());
            keyByName.remove(oldName);

            IDevice newDevice = new Device()
                    .setIp(oldDevice.getIp())
                    .setMac(oldDevice.getMac())
                    .setName(newName);
            String newKey = buildDeviceKey(newDevice);
            devicesByKey.put(newKey, newDevice);
            keyByIp.put(newDevice.getIp(), newKey);
            keyByMac.put(newDevice.getMac(), newKey);
            keyByName.put(newDevice.getName(), newKey);
        }
        persistRepository();
    }

    public @NotNull IDevice getDevice(String ipOrName, boolean includeState, boolean useCache) throws UnknownDeviceException {
        if (StringUtils.isBlank(ipOrName)) {
            throw new IllegalArgumentException("Device ipOrName cannot be blank");
        }
        IDevice device = getDeviceFromString(ipOrName);
        if (device == null) {
            log.info("{} does not map to a device", ipOrName);
            throw new UnknownDeviceException(ipOrName);
        }

        if(! includeState) {
            device = new Device(device);
        } else if(!useCache || !(device instanceof DeviceWithState)) {
            try {
                device = new DeviceWithState(device).setDeviceState(lightbridgeService.queryState(device.getIp()));
                devicesByKey.put(buildDeviceKey(device), device);
            } catch (MessageException | DeviceException e) {
                log.error("While getting device: {}", e.getMessage(), e);
                device = new DeviceWithMessage(device, e.getMessage());
            }
        }

        return device;
    }

    private IDevice getDeviceFromString(String ipOrName) {
        try {
            validateIp(ipOrName);
            return devicesByKey.get(keyByIp.get(ipOrName));
        } catch (InvalidIpAddressException e) {
            return devicesByKey.get(keyByName.get(ipOrName));
        }
    }

    public Map<String, IDevice> getDevices(boolean includeState, boolean useCache) {
        Map<String, IDevice> result;
        result = devicesByKey.entrySet().stream()
                .map(entry -> {
                    if (!includeState) {
                        // strip out potential state by converting to devices
                        return new AbstractMap.SimpleEntry<String, IDevice>(entry.getKey(), new Device(entry.getValue()));
                    } else if (useCache) {
                        IDevice iDevice = entry.getValue();
                        if (!(iDevice instanceof DeviceWithState)) { // no state included
                            return getDeviceWithFreshStateEntry(entry.getKey(), iDevice);
                        } else { // we already have cached state
                            return entry;
                        }
                    } else {
                        // override cachedState
                        IDevice iDevice = entry.getValue();
                        return getDeviceWithFreshStateEntry(entry.getKey(), iDevice);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return Collections.unmodifiableMap(result);
    }

    private Map.Entry<String, ? extends IDevice> getDeviceWithFreshStateEntry(String key, IDevice iDevice) {
        try {
            DeviceState state = lightbridgeService.queryState(iDevice.getIp());
            DeviceWithState deviceWithState = new DeviceWithState(iDevice)
                    .setDeviceState(state);
            devicesByKey.put(key, deviceWithState);
            return new AbstractMap.SimpleEntry<>(key,
                   deviceWithState);
        } catch (Exception e) {
            String message = e.getMessage();
            log.error("While querying device state for {}: {}", key, message, e);
            return new AbstractMap.SimpleEntry<>(key,
                    new DeviceWithMessage(iDevice, message));
        }
    }

    private void initialize() throws IOException, DiscoveryException, DeviceConflictException {
        File f = new File(deviceFile);
        if (f.isFile() && f.canRead()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                devicesByKey = new TreeMap<>(objectMapper.readValue(fis.readAllBytes(), new TypeReference<Map<String, Device>>() {
                }));
            }
        } else {
            devicesByKey = new TreeMap<>();
            Map<String, IDevice> devices = lightbridgeService.discover();
            clearDevices();
            if (devices == null) {
                throw new IllegalArgumentException("Devices map cannot be null");
            }
            for (IDevice device : devices.values()) {
                addDevice(device);
            }
        }
        devicesByKey.forEach((key, value) -> {
            keyByIp.put(value.getIp(), key);
            keyByMac.put(value.getMac(), key);
            keyByName.put(value.getName(), key);
        });
    }

    private void persistRepository() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(deviceFile))) {
            fos.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(devicesByKey));
        }
    }

    private void validateIp(String ip) throws InvalidIpAddressException {
        if (StringUtils.isBlank(ip)) {
            throw new InvalidIpAddressException(ip);
        }

        String[] tokens = ip.split("\\.");

        if (tokens.length != 4) {
            throw new InvalidIpAddressException(ip);
        }

        for (String token : tokens) {
            try {
                int quad = Integer.parseInt(token);
                if (quad < 0 || quad > 254) {
                    throw new InvalidIpAddressException(ip);
                }
            } catch (NumberFormatException e) {
                throw new InvalidIpAddressException(ip);
            }
        }
    }

    public String buildDeviceKey(IDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        return device.getName() + "_" + device.getMac();
    }
}
