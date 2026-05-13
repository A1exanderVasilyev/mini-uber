package com.miniuber.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Response received from location service
 * for nearby driver query
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearbyDriverResponse {
    private String driverId;
    private double longitude;
    private double latitude;
    private double distanceInKm;
}
