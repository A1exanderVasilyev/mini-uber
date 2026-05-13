package com.miniuber.matchingservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published by ride service
 * Consumed from kafka topic: ride.requested
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestedEvent {
    private Long rideId;
    private String riderId;

    private double pickupLongitude;
    private double pickupLatitude;
    private String pickupAddress;

    private double dropOffLongitude;
    private double dropOffLatitude;
    private String dropOffAddress;
}
