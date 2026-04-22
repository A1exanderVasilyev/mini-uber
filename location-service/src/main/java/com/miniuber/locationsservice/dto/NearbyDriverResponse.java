package com.miniuber.locationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearbyDriverResponse {
    private String driverId;
    private double longitude;
    private double altitude;
    private String distanceInKm;
}
