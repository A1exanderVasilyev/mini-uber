package com.miniuber.locationsservice.controllers;

import com.miniuber.locationsservice.dto.DriverLocationRequest;
import com.miniuber.locationsservice.dto.NearbyDriverResponse;
import com.miniuber.locationsservice.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private static final String DEFAULT_RADIUS_IN_KM = "5.0";

    @Autowired
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // called by drivers app every 3 seconds
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(
            @RequestBody DriverLocationRequest request
    ) {
        locationService.updateDriverLocation(request);
        return ResponseEntity.ok("Driver location updated");
    }

    // called by matching service when requested a ride
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> getNearbyDrivers(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = DEFAULT_RADIUS_IN_KM) double radius
    ) {
        return ResponseEntity.ok(locationService.findNearbyDrivers(longitude, latitude, radius));
    }

    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverId) {
        locationService.remove(driverId);
        return ResponseEntity.ok("Driver removed");
    }
}
