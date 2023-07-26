package com.meesho.perfcomparison.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.repositories.DataRepository;
import com.meesho.perfcomparison.services.CacheService;
import com.meesho.perfcomparison.services.MqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Slf4j
@Service
public class KafkaMqService implements MqService {
    final KafkaSender kafkaSender;
    final ObjectMapper objectMapper;

    @Value("${kafka.topic}")
    private String topic;

    // Add a constructor to create a kafka producer fetching producer configurations from the application.yml file
    public KafkaMqService(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
                          @Value("${kafka.acks}") String acks,
                          @Value("${kafka.retries}") String retries,
                          @Value("${kafka.batch-size}") String batchSize,
                          @Value("${kafka.linger-ms}") String lingerMs,
                          @Value("${kafka.buffer-memory}") String bufferMemory,
                          @Value("${kafka.key-serializer}") String keySerializer,
                          @Value("${kafka.value-serializer}") String valueSerializer,
                          @Value("${kafka.max-in-flight-messages}") Integer maxInFlightMessages) {
        Map<String, Object> producerConfigs = Map.of(
                "bootstrap.servers", bootstrapServers,
                "acks", acks,
                "retries", retries,
                "batch.size", batchSize,
                "linger.ms", lingerMs,
                "buffer.memory", bufferMemory,
                "key.serializer", keySerializer,
                "value.serializer", valueSerializer,
                "compression.type", "zstd"
        );
        SenderOptions<String, String> senderOptions = SenderOptions.create(producerConfigs);

        senderOptions.maxInFlight(maxInFlightMessages);
        this.kafkaSender = KafkaSender.create(senderOptions);
        this.objectMapper = new ObjectMapper();
    }



    @Override
    public Mono<Void> sendData(Flux<Data> data) {
        return this.kafkaSender
                .send(data.map(record -> {
                    try {
                        return this.objectMapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        log.error("Error while converting to json", e);
                        return null;
                    }
                }).map(stringRecord -> new ProducerRecord(topic, stringRecord)))
                .doOnError(e -> log.error("Error while sending data to kafka", e))
                .then();
    }
}
