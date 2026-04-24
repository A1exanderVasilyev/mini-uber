package com.miniuber.rideservice.services;

import com.miniuber.rideservice.dto.RideRequest;
import com.miniuber.rideservice.dto.RideResponse;
import com.miniuber.rideservice.events.RideRequestedEvent;
import com.miniuber.rideservice.models.Ride;
import com.miniuber.rideservice.models.RideStatus;
import com.miniuber.rideservice.repositories.RideRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class RideService {
    @Value("${fare.base-price:50}")
    private double basePrice;
    @Value("${fare.price-per-km:12}")
    private double pricePerKm;

    private static final double EARTH_RADIUS_M = 6371000.0;

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private final String RIDE_REQUESTED_TOPIC = "ride.requested";

    public RideService(RideRepository rideRepository, KafkaTemplate<String, RideRequestedEvent> kafkaTemplate) {
        this.rideRepository = rideRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public RideResponse requestRide(RideRequest rideRequest) {
        log.info("New ride requested from rider: {}", rideRequest.getRiderId());
        Ride rideToSave = new Ride();
        rideToSave.setRiderId(rideRequest.getRiderId());
        rideToSave.setPickupLongitude(rideRequest.getPickupLongitude());
        rideToSave.setPickupLatitude(rideRequest.getPickupLatitude());
        rideToSave.setPickupAddress(rideRequest.getPickupAddress());
        rideToSave.setDropOffLongitude(rideRequest.getDropOffLongitude());
        rideToSave.setDropOffLatitude(rideRequest.getDropOffLatitude());
        rideToSave.setDropOffAddress(rideRequest.getDropOffAddress());
        rideToSave.setStatus(RideStatus.REQUESTED);
        rideToSave.setEstimatedFare(calculateEstimateFare(rideRequest));

        Ride savedRide = rideRepository.save(rideToSave);

        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropOffLongitude(),
                savedRide.getDropOffLatitude(),
                savedRide.getDropOffAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, String.valueOf(savedRide.getId()), event);
        log.info("Ride published to Kafka for ride: {}", savedRide.getId());

        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);
        return rideToResponseMapper(savedRide);
    }

    private RideResponse rideToResponseMapper(Ride savedRide) {
        RideResponse response = new RideResponse();
        response.setId(savedRide.getId());
        response.setRiderId(savedRide.getRiderId());
        response.setDriverId(savedRide.getDriverId());

        response.setPickupLongitude(savedRide.getPickupLongitude());
        response.setPickupLatitude(savedRide.getPickupLatitude());
        response.setPickupAddress(savedRide.getPickupAddress());

        response.setDropOffLongitude(savedRide.getDropOffLongitude());
        response.setDropOffLatitude(savedRide.getDropOffLatitude());
        response.setDropOffAddress(savedRide.getDropOffAddress());

        response.setStatus(savedRide.getStatus());
        response.setEstimatedFare(savedRide.getEstimatedFare());
        response.setActualFare(savedRide.getActualFare());

        response.setCreatedAt(savedRide.getCreatedAt());
        response.setUpdatedAt(savedRide.getUpdatedAt());
        response.setStartedAt(savedRide.getStartedAt());
        response.setCompletedAt(savedRide.getCompletedAt());

        return response;
    }

    public void driverAcceptedRideUpdate(Long rideId, String driverId) {
        Ride rideToUpdate = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        rideToUpdate.setDriverId(driverId);
        rideToUpdate.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(rideToUpdate);
    }

    private double calculateEstimateFare(RideRequest rideRequest) {

        double distanceInKm = calculateHaversineDistance(
                rideRequest.getPickupLatitude(), rideRequest.getPickupLongitude(),
                rideRequest.getDropOffLatitude(), rideRequest.getDropOffLongitude()
        );

        double fare = basePrice + (distanceInKm * pricePerKm);
        return Math.round(fare * 100.0) / 100.0;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {

        double radLat1 = Math.toRadians(lat1);
        double radLon1 = Math.toRadians(lon1);
        double radLat2 = Math.toRadians(lat2);
        double radLon2 = Math.toRadians(lon2);

        double latDiff = radLat2 - radLat1;
        double lonDiff = radLon2 - radLon1;

        double a = Math.pow(Math.sin(latDiff / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(lonDiff / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInMeters = EARTH_RADIUS_M * c;
        return distanceInMeters / 1000.0;
    }

    public RideResponse startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride can not be started. Current status is " + ride.getStatus());
        }

        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);
        return rideToResponseMapper(ride);
    }

    public RideResponse completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        if (ride.getStatus() != RideStatus.RIDE_STARTED) {
            throw new RuntimeException("Ride can not be completed. Current status is " + ride.getStatus());
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);
        return rideToResponseMapper(ride);
    }

    public RideResponse cancelRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setStatus(RideStatus.CANCELLED);
        ride.setUpdatedAt(LocalDateTime.now());
        rideRepository.save(ride);
        return rideToResponseMapper(ride);
    }

    public RideResponse getRideById(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        return rideToResponseMapper(ride);
    }

    public List<RideResponse> getRidesByRider(Long riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::rideToResponseMapper)
                .toList();
    }
}
