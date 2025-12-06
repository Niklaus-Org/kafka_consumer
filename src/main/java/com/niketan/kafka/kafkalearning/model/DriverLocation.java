package com.niketan.kafka.kafkalearning.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DriverLocation {
    
    private String driverId;
    private double latitude;
    private double longitude;
    private String location;
}
