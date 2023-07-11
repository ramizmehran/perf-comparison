package com.meesho.perfcomparison.repositories;

import com.meesho.perfcomparison.entities.Data;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DataRepository extends R2dbcRepository<Data, Integer> {
}
