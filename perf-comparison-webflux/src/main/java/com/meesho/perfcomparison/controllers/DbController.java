package com.meesho.perfcomparison.controllers;

import com.meesho.perfcomparison.entities.Data;
import com.meesho.perfcomparison.services.DbService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("db")
public class DbController {
    final DbService dbService;

    public DbController(DbService dbService) {
        this.dbService = dbService;
    }

    @GetMapping("{id}")
    public Mono<Data> fetchData(@PathVariable("id") Integer id) {
        return dbService.fetchData(id);
    }

    @PostMapping
    public Mono<Data> insertData(@RequestBody Data data) {
        return dbService.insertData(data);
    }
}
