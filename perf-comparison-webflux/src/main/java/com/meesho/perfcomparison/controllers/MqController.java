package com.meesho.perfcomparison.controllers;

import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.services.MqService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("cache")
public class MqController {
    final MqService mqService;

    public MqController(MqService mqService) {
        this.mqService = mqService;
    }


    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<Void> events(
            @RequestBody Flux<Data> payloads) {

        return mqService.sendData(payloads);
    }
}
