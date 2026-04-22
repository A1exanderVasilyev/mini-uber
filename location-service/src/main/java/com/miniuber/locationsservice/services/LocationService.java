package com.miniuber.locationsservice.services;

import com.miniuber.locationsservice.dto.DriverLocationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocationService {

    // redis key for all driver locations;
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    private final RedisTemplate<String, String> redisTemplate;

    public LocationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Updates driver location in Redis every 3 seconds
     * Maps with GEOADD redis command;
     */
    public void updateDriverLocation(DriverLocationRequest request) {
        log.info("Updating location for driver: {}", request.getDriverId());
        Point driverPoint = new Point(
                request.getLongitude(),
                request.getLatitude()
        );
        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                request.getDriverId()
        );
        log.info("Location updated for driver: {}", request.getDriverId());
    }
}
