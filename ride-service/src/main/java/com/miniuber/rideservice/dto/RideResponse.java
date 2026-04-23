package com.miniuber.rideservice.dto;

import com.miniuber.rideservice.models.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideResponse {
    private Long id;
    private String riderId;
    private String driverId;

    private double pickupLongitude;
    private double pickupLatitude;
    private String pickupAddress;

    private double dropOffLongitude;
    private double dropOffLatitude;
    private String dropOffAddress;

    private RideStatus status;

    private double estimatedFare;
    private double actualFare;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
