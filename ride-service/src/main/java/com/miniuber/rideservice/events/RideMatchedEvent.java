package com.miniuber.rideservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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