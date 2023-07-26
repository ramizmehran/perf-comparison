package com.meesho.perfcomparison.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.repositories.DataRepository;
import com.meesho.perfcomparison.services.CacheService;
import com.meesho.perfcomparison.services.DbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RedisCacheService implements CacheService {
    final ObjectMapper mapper = new ObjectMapper();
    final DataRepository dataRepository;
    final ReactiveRedisTemplate<Integer, String> reactiveRedisTemplate;

    public RedisCacheService(DataRepository dataRepository, ReactiveRedisTemplate<Integer, String> reactiveRedisTemplate) {
        this.dataRepository = dataRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<Data> fetchData(Integer id) {
        return reactiveRedisTemplate
                .opsForValue()
                .get(id)
                .switchIfEmpty(
                        dataRepository
                                .findById(id)
                                .<String>handle((data, sink) -> {
                                    try {
                                        sink.next(mapper.writeValueAsString(data));
                                    } catch (JsonProcessingException e) {
                                        sink.error(new RuntimeException("Error while inserting data in redis", e));
                                    }
                                })
                                .doOnNext(data -> reactiveRedisTemplate.opsForValue().set(id, data))
                ).handle((data, sink) -> {
                    try {
                        sink.next(mapper.readValue(data, Data.class));
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                    }
                });
    }
}
