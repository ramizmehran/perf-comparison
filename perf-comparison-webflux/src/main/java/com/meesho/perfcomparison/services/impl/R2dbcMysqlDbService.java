package com.meesho.perfcomparison.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.repositories.DataRepository;
import com.meesho.perfcomparison.services.DbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
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
    public Mono<Data> insertData(Data data) {
        return dataRepository
                .save(data)
                .doOnNext(data1 -> {
                    try {
                        reactiveRedisTemplate.opsForValue().set(data1.getId(), mapper.writeValueAsString(data1));
                    } catch (JsonProcessingException e) {
                        log.error("Error while inserting data in redis", e);
                    }
                });
    }
}
