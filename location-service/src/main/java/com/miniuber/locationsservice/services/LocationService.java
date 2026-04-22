package com.miniuber.locationsservice.services;

import com.miniuber.locationsservice.dto.DriverLocationRequest;
import com.miniuber.locationsservice.dto.NearbyDriverResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Maps with GEORADIUS redis command
     */
    public @Nullable List<NearbyDriverResponse> findNearbyDrivers(double longitude, double latitude, double radius) {
        log.info("Finding drivers nearby with latitude: {}, longitude: {}, with radius: {}", latitude, longitude, radius);
        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radius, Metrics.KILOMETERS));

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVERS_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();
        if (results != null) {
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearbyDrivers.add(new NearbyDriverResponse(
                        location.getName(),
                        location.getPoint().getX(),
                        location.getPoint().getY(),
                        result.getDistance().getValue()
                ));
            });
            log.info("Found {} drivers nearby", nearbyDrivers.size());
        } else {
            log.info("No drivers nearby found");
        }

        return nearbyDrivers;
    }

    /**
     * Removes driver when he offline
     * Maps to ZREM redis command
     */
    public void remove(String driverId) {
        log.info("Removing driver: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
        log.info("Driver removed: {}", driverId);
    }
}
