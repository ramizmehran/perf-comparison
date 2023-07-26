package com.meesho.perfcomparison.controllers;

import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.services.CacheService;
import com.meesho.perfcomparison.services.DbService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("cache")
public class RedisController {
    final CacheService cacheService;

    public RedisController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("{id}")
    public Mono<Data> fetchData(@PathVariable("id") Integer id) {
        return cacheService.fetchData(id);
    }
}
