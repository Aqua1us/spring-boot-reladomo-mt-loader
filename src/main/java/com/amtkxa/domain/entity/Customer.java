package com.amtkxa.domain.entity;

import java.sql.Timestamp;

public class Customer extends CustomerAbstract {
    public Customer(Timestamp businessDate, Timestamp processingDate) {
        super(businessDate, processingDate);
        // You must not modify this constructor. Mithra calls this internally.
        // You can call this constructor. You can also add new constructors.
    }

    public Customer(Timestamp businessDate) {
        super(businessDate);
    }

    public Customer(String name, String country, Timestamp businessDate) {
        super(businessDate);
        super.setName(name);
        super.setCountry(country);
    }

    public Customer(int customerId, String name, String country, Timestamp businessDate) {
        super(businessDate);
        super.setCustomerId(customerId);
        super.setName(name);
        super.setCountry(country);
    }

}
