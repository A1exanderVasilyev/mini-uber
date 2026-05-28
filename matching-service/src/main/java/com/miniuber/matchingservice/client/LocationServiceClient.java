package com.miniuber.matchingservice.client;

import com.miniuber.matchingservice.dto.NearbyDriverResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;


@Service
public class LocationServiceClient {
    private final RestClient restClient;

    public LocationServiceClient(@Value("${location.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<NearbyDriverResponse> getNearbyDrivers(double longitude, double latitude, double radius) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/locations/drivers/nearby")
                        .queryParam("longitude", longitude)
                        .queryParam("latitude", latitude)
                        .queryParam("radius", radius)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
