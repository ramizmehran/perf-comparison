package com.meesho.perfcomparison.entities;

import org.springframework.data.annotation.Id;

@lombok.Data
public class Data {
    @Id
    private Integer id;
    private String name;
    private String email;
}
