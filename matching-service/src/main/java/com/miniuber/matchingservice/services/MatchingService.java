package com.miniuber.matchingservice.services;

import com.miniuber.matchingservice.client.LocationServiceClient;
import com.miniuber.matchingservice.dto.NearbyDriverResponse;
import com.miniuber.matchingservice.events.RideMatchedEvent;
import com.miniuber.matchingservice.events.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     * Matching algorithm
     * Called when RideRequestEvent is consumed
     * Steps:
     * 1. Ask for nearby drivers
     * 2. Score each nearby driver and pick the best
     * 3. Send event to Kafka topic
     */

    public void matchDriverForRide(RideRequestedEvent event) {
        List<NearbyDriverResponse> nearbyDrivers = locationServiceClient.getNearbyDrivers(
                event.getPickupLongitude(),
                event.getPickupLatitude(),
                DEFAULT_SEARCH_RADIUS_KM);

        if (nearbyDrivers.isEmpty()) {
            log.warn("No nearby drivers found for ride");
            return;
        }

        Optional<NearbyDriverResponse> bestDriver = findBestDriver(nearbyDrivers);
        if (bestDriver.isEmpty()) {
            log.warn("No best driver found for ride");
            return;
        }

        NearbyDriverResponse assignedDriver = bestDriver.get();

        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLongitude(),
                assignedDriver.getLatitude(),
                assignedDriver.getDistanceInKm()
        );

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, String.valueOf(event.getRideId()), matchedEvent);
        log.info("Ride matched event sent to topic");
    }

    /**
     * Scoring algorithm
     * Distance weight - 70%
     * Rating - 30%
     * distance for scoring is inversional value (if low dist - value bigger)
     *
     * @param nearbyDrivers
     */
    private Optional<NearbyDriverResponse> findBestDriver(List<NearbyDriverResponse> nearbyDrivers) {
        double distanceWeight = 0.7;
        double ratingWeight = 0.3;
        double minDistance = 0.1;
        return nearbyDrivers.stream()
                .max(Comparator.comparingDouble((driver) -> {
                    double distanceScore = 1.0 / (driver.getDistanceInKm() + minDistance);

                    // TODO: driver service for rating fetch
                    double rating = 4.0 + Math.random();

                    return distanceScore * distanceWeight + rating * ratingWeight;
                }));
    }
}
