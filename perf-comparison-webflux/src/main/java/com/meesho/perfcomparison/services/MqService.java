package com.meesho.perfcomparison.services;

import com.meesho.perfcomparison.entities.Data;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface MqService {
    Mono<Void> sendData(Flux<Data> data);

}
