package com.niketan.kafka.kafkalearning.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.niketan.kafka.kafkalearning.model.DriverLocation;

@Service
public class KafkaConsumer {
    
    @KafkaListener(topics = "driver-location-updates", groupId = "driver-location-group")
    public void consumeRecord(ConsumerRecord<String, Object> record) {
        System.out.println("Consumed record with key: " + record.key() + " and value: " + record.value().toString());
    }
    
}
