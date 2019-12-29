package com.amtkxa.presentation.entity;

import com.opencsv.bean.CsvBindByPosition;

public class CustomerCsvEntity {

    @CsvBindByPosition(position = 0, required = true)
    private int customerId;
    @CsvBindByPosition(position = 1, required = true)
    private String name;
    @CsvBindByPosition(position = 2, required = true)
    private String country;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
