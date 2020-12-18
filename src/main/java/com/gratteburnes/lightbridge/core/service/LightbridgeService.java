package com.gratteburnes.lightbridge.core.service;

import com.gratteburnes.magichome.model.DeviceState;
import com.gratteburnes.magichome.model.IDevice;
import com.gratteburnes.magichome.model.error.DeviceException;
import com.gratteburnes.magichome.model.error.DiscoveryException;
import com.gratteburnes.magichome.model.error.MessageException;
import com.gratteburnes.magichome.service.DiscoveryService;
import com.gratteburnes.magichome.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class LightbridgeService {
    private final DiscoveryService discoveryService;

    private final MessageService messageService;

    @Value("${lightbridge.discovery.timeout.seconds:5}")
    private int timeout;

    public LightbridgeService(DiscoveryService discoveryService, MessageService messageService) {
        this.discoveryService = discoveryService;
        this.messageService = messageService;
    }

    @Retryable(value = IOException.class, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.backoff.seconds}"))
    public Map<String, IDevice> discover() throws DiscoveryException {
        return discoveryService.discover(timeout);
    }

    @Retryable(value = IOException.class, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.backoff.seconds}"))
    public boolean turnDeviceOn(String ip) throws MessageException {
        return messageService.turnDeviceOn(ip);
    }

    @Retryable(value = IOException.class, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.backoff.seconds}"))
    public boolean turnDeviceOff(String ip) throws MessageException {
        return messageService.turnDeviceOff(ip);
    }

    @Retryable(value = IOException.class, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.backoff.seconds}"))
    public boolean changeDeviceColor(int red, int green, int blue, int warmWhite, int coolWhite, String ip) throws MessageException, DeviceException {
        return messageService.changeDeviceColor(red, green, blue, warmWhite, coolWhite, ip);
    }

    @Retryable(value = IOException.class, maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.backoff.seconds}"))
    public DeviceState queryState(String ip) throws MessageException, DeviceException {
        return messageService.queryState(ip);
    }
}
