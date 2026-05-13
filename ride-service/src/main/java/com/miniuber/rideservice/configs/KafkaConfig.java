package com.miniuber.rideservice.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    // topic for matching service to subscribe
    @Bean
    public NewTopic rideRequestTopic() {
        return TopicBuilder.name("ride.requested")
                .partitions(3)
                .replicas(1)
                .build();
    }

    //topic for ride service to subscribe for ride match result
    @Bean
    public NewTopic rideResponseTopic() {
        return TopicBuilder.name("ride.matched")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
