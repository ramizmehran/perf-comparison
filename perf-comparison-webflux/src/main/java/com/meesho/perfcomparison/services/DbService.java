package com.meesho.perfcomparison.services;

import com.meesho.perfcomparison.entities.Data;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface DbService {
    Mono<Data> fetchData(Integer id);

    Mono<Data> insertData(Data data);
}
