package com.miniuber.matchingservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consumed by ride service to update ride with assigned driver
 * Published to kafka topic: ride.matched
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideMatchedEvent {
    private Long rideId;
    private String riderId;
    private String driverId;

    private double driverLongitude;
    private double driverLatitude;
    private double distanceToPickupKm;
}
