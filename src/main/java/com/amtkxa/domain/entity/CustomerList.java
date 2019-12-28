package com.amtkxa.domain.entity;

import java.util.Collection;

import com.gs.fw.finder.Operation;

public class CustomerList extends CustomerListAbstract {
    public CustomerList() {
        super();
    }

    public CustomerList(int initialSize) {
        super(initialSize);
    }

    public CustomerList(Collection c) {
        super(c);
    }

    public CustomerList(Operation operation) {
        super(operation);
    }
}
