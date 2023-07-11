package com.meesho.perfcomparison.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.repositories.DataRepository;
import com.meesho.perfcomparison.services.DbService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class R2dbcMysqlDbService implements DbService {
    final ObjectMapper mapper = new ObjectMapper();
    final DataRepository dataRepository;
    final ReactiveRedisTemplate<Integer, String> reactiveRedisTemplate;

    public R2dbcMysqlDbService(DataRepository dataRepository, ReactiveRedisTemplate<Integer, String> reactiveRedisTemplate) {
        this.dataRepository = dataRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<Data> fetchData(Integer id) {
        return dataRepository.findById(id);
    }

    @Override
    public Mono<Data> fetchCachedData(Integer id) {
        return reactiveRedisTemplate
                .opsForValue()
                .get(id)
                .switchIfEmpty(dataRepository.findById(id).flatMap(data -> {
                    try {
                        return reactiveRedisTemplate.opsForValue().set(id, mapper.writeValueAsString(data));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                }).then(reactiveRedisTemplate.opsForValue().get(id)))
                .map(data -> {
                    try {
                        return mapper.readValue(data, Data.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
