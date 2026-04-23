package com.miniuber.rideservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequest {

    @NotBlank(message = "Rider id is required")
    private String riderId;

    @NotNull(message = "Pickup longitude is required")
    private double pickupLongitude;

    @NotNull(message = "Pickup latitude is required")
    private double pickupLatitude;

    @NotNull(message = "Pickup address is required")
    private String pickupAddress;

    @NotNull(message = "Drop off longitude is required")
    private double dropOffLongitude;

    @NotNull(message = "Drop off latitude is required")
    private double dropOffLatitude;

    @NotNull(message = "Drop off address is required")
    private String dropOffAddress;
}
