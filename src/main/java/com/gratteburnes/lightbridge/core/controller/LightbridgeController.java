package com.gratteburnes.lightbridge.core.controller;

import com.gratteburnes.lightbridge.core.controller.contract.ILightBridgeV1Contract;
import com.gratteburnes.lightbridge.core.model.DeviceWithState;
import com.gratteburnes.lightbridge.core.model.DiscoveredDevice;
import com.gratteburnes.lightbridge.core.model.DevicesByName;
import com.gratteburnes.lightbridge.core.model.RgbWWCWColor;
import com.gratteburnes.lightbridge.core.model.error.DeviceConflictException;
import com.gratteburnes.lightbridge.core.model.error.UnknownDeviceException;
import com.gratteburnes.lightbridge.core.service.DeviceRepositoryService;
import com.gratteburnes.lightbridge.core.service.LightbridgeService;
import com.gratteburnes.magichome.model.DeviceState;
import com.gratteburnes.magichome.model.IDevice;
import com.gratteburnes.magichome.model.error.DeviceException;
import com.gratteburnes.magichome.model.error.DiscoveryException;
import com.gratteburnes.magichome.model.error.MessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/lightbridge/v1/")
@Slf4j
public class LightbridgeController implements ILightBridgeV1Contract {
    private final LightbridgeService lightbridgeService;
    private final DeviceRepositoryService deviceRepositoryService;

    public LightbridgeController(LightbridgeService lightbridgeService, DeviceRepositoryService deviceRepositoryService, @Value("${lightbridge.persistence.location}") String deviceFile) throws DiscoveryException, IOException, DeviceConflictException {
        this.lightbridgeService = lightbridgeService;
        this.deviceRepositoryService = deviceRepositoryService;
    }

    @PutMapping("/discover")
    public DevicesByName discoverDevices() throws DiscoveryException {
        Map<String, IDevice> discovered = lightbridgeService.discover();
        Map<String, IDevice> result = new TreeMap<>();
        discovered.keySet().forEach((key -> {
            IDevice device = discovered.get(key);
            boolean added = false;
            String message = "";
            try {
                deviceRepositoryService.addDevice(device);
                added = true;
            } catch (DeviceConflictException e) {
                message = e.getMessage();
                log.warn("While discovering devices: {}", message);
            } catch (IOException e) {
                message = e.getMessage();
                log.error("While discovering devices: {}", message, e);
            }
            result.put(deviceRepositoryService.buildDeviceKey(device), new DiscoveredDevice(device, added, message));
        }));
        return new DevicesByName(result);
    }

    @PutMapping("/{ipOrName}/on")
    public DeviceWithState on(@PathVariable("ipOrName") String ipOrName) throws MessageException, DeviceException {
        IDevice device = deviceRepositoryService.getDevice(ipOrName, false, false);
        if (lightbridgeService.turnDeviceOn(device.getIp())) {
            return new DeviceWithState(device).setDeviceState(lightbridgeService.queryState(device.getIp()));
        } else {
            throw new DeviceException(ipOrName + " could not be turned on");
        }
    }

    @PutMapping("/{ipOrName}/off")
    public DeviceWithState off(@PathVariable("ipOrName") String ipOrName) throws MessageException, DeviceException {
        IDevice device = deviceRepositoryService.getDevice(ipOrName, false, false);
        if (lightbridgeService.turnDeviceOff(device.getIp())) {
            return new DeviceWithState(device).setDeviceState(lightbridgeService.queryState(device.getIp()));
        } else {
            throw new DeviceException(ipOrName + " could not be turned off");
        }
    }

    @PutMapping("/{ipOrName}/color")
    public DeviceWithState color(@PathVariable("ipOrName") String ipOrName, @NotNull @RequestBody RgbWWCWColor rgbWWCWColor) throws MessageException, DeviceException {
        IDevice device = deviceRepositoryService.getDevice(ipOrName, false, false);
        if (lightbridgeService.changeDeviceColor(rgbWWCWColor.getRed(), rgbWWCWColor.getGreen(), rgbWWCWColor.getBlue(), rgbWWCWColor.getWarmWhite(), rgbWWCWColor.getCoolWhite(), device.getIp())) {
            return new DeviceWithState(device).setDeviceState(lightbridgeService.queryState(device.getIp()));
        } else {
            throw new DeviceException(ipOrName + " color could not be changed");
        }
    }

    @PutMapping("/device/rename/{ipOrName}/{newName}")
    public String rename(@NotBlank @PathVariable("ipOrName") String ipOrName, @NotBlank @PathVariable("newName") String newName) throws IOException, UnknownDeviceException {
        deviceRepositoryService.renameDevice(ipOrName, newName);
        return ipOrName + " was renamed to " + newName;
    }

    @GetMapping("/device/{ipOrName}")
    public IDevice getDevice(@NotBlank @PathVariable("ipOrName") String ipOrName,
                             @RequestParam(value = "state", required = false, defaultValue = "false") boolean includeState,
                             @RequestParam(value = "cache", required = false, defaultValue = "false") boolean useCache) throws UnknownDeviceException {
        return deviceRepositoryService.getDevice(ipOrName, includeState, useCache);
    }

    @DeleteMapping("/device/{ipOrName}")
    public IDevice deleteDevice(@NotBlank @PathVariable("ipOrName") String ipOrName) throws UnknownDeviceException, IOException {
        return deviceRepositoryService.removeDevice(ipOrName);
    }

    @GetMapping("/devices")
    public DevicesByName getDevices(@RequestParam(value = "state", required = false, defaultValue = "false") boolean includeState,
                                           @RequestParam(value = "cache", required = false, defaultValue = "false") boolean useCache) {
        return new DevicesByName(deviceRepositoryService.getDevices(includeState, useCache));
    }

    @GetMapping("/{ipOrName}/state")
    public @NotNull DeviceWithState getDeviceState(@NotBlank @PathVariable("ipOrName") String ipOrName) throws MessageException, DeviceException {
        IDevice device = deviceRepositoryService.getDevice(ipOrName, true, false);
        DeviceState state = device instanceof DeviceWithState ?
                ((DeviceWithState)device).getDeviceState() :
                lightbridgeService.queryState(device.getIp());
        return new DeviceWithState(device)
                .setDeviceState(state);
    }
}
