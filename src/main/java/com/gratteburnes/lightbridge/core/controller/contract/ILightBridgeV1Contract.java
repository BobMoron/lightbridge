package com.gratteburnes.lightbridge.core.controller.contract;

import com.gratteburnes.lightbridge.core.model.DeviceWithState;
import com.gratteburnes.lightbridge.core.model.DevicesByName;
import com.gratteburnes.lightbridge.core.model.RgbWWCWColor;
import com.gratteburnes.lightbridge.core.model.error.UnknownDeviceException;
import com.gratteburnes.magichome.model.DeviceState;
import com.gratteburnes.magichome.model.IDevice;
import com.gratteburnes.magichome.model.error.DeviceException;
import com.gratteburnes.magichome.model.error.DiscoveryException;
import com.gratteburnes.magichome.model.error.MessageException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

@RequestMapping("/lightbridge/v1/")
@Tag(name = "lightbridge", description = "Magic home device bridge controller")
public interface ILightBridgeV1Contract {
    @PutMapping("/discover")
    @Operation(summary = "Triggers device discovery", description = "trigger device discovery", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DevicesByName.class)))})
    DevicesByName discoverDevices() throws DiscoveryException;

    @PutMapping("/{ipOrName}/on")
    @Operation(summary = "Turns device on", description = "turn device on by device name or IP address", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DeviceWithState.class)))})
    DeviceWithState on(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName) throws MessageException, DeviceException;

    @PutMapping("/{ipOrName}/off")
    @Operation(summary = "Turns device off", description = "turn device off by device name or IP address", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DeviceWithState.class)))})
    DeviceWithState off(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName) throws MessageException, DeviceException;

    @PutMapping("/{ipOrName}/color")
    @Operation(summary = "Sets device color", description = "set device color by device name or IP address", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DeviceWithState.class)))})
    DeviceWithState color(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName,
                          @RequestBody(description = "RGB WW CW color", required = true) RgbWWCWColor rgbWWCWColor) throws MessageException, DeviceException;

    @PutMapping("/device/rename/{ipOrName}/{newName}")
    @Operation(summary = "Renames device", description = "rename device by device name or IP address", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = String.class)))})
    String rename(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName,
                  @Parameter(in = ParameterIn.PATH, name = "newName", description = "New device name", required = true)  String newName) throws IOException, UnknownDeviceException;

    @GetMapping("/device/{ipOrName}")
    @Operation(summary = "Gets device record", description = "get device record", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = IDevice.class)))})
    IDevice getDevice(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName,
                      @Parameter(in = ParameterIn.QUERY, name = "state", description = "include device state, defaults to false", required = false) boolean includeState,
                      @Parameter(in = ParameterIn.QUERY, name = "cache", description = "use device cache, defaults to false", required = false) boolean useCache) throws UnknownDeviceException;

    @DeleteMapping("/device/{ipOrName}")
    @Operation(summary = "Deletes device record", description = "delete device record", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = IDevice.class)))})
    IDevice deleteDevice(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName) throws UnknownDeviceException, IOException;

    @GetMapping("/devices")
    @Operation(summary = "Gets all device records", description = "get all device records", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DevicesByName.class)))})
    DevicesByName getDevices(@Parameter(in = ParameterIn.QUERY, name = "state", description = "include device state, defaults to false", required = false) boolean includeState,
                             @Parameter(in = ParameterIn.QUERY, name = "cache", description = "use device cache, defaults to false", required = false) boolean useCache);

    @GetMapping("/{ipOrName}/state")
    @Operation(summary = "Gets device state", description = "gets device state by device name or IP address", tags = {"lightbridge"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DeviceWithState.class)))})
    DeviceWithState getDeviceState(@Parameter(in = ParameterIn.PATH, name = "ipOrName", description = "Device name or IP address", required = true) String ipOrName) throws MessageException, DeviceException;
}
