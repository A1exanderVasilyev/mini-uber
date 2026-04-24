package com.miniuber.rideservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This event is published to kafka when a ride requested
 * matching service will consume event with topic: 'ride.requested'
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
