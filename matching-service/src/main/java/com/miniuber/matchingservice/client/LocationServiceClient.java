package com.miniuber.matchingservice.client;

import com.miniuber.matchingservice.dto.NearbyDriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "location-service", url = "${location.service.url}")
public interface LocationServiceClient {

    @GetMapping("/api/v1/locations/drivers/nearby")
    List<NearbyDriverResponse> getNearbyDrivers(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam double radius
    );
}