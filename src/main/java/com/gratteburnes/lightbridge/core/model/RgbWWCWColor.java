package com.gratteburnes.lightbridge.core.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class RgbWWCWColor {
    @Max(255)
    @Min(0)
    private int red;
    @Max(255)
    @Min(0)
    private int green;
    @Max(255)
    @Min(0)
    private int blue;
    @Max(255)
    @Min(0)
    private int coolWhite;
    @Max(255)
    @Min(0)
    private int warmWhite;
}
