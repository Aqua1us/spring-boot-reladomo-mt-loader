package com.amtkxa.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amtkxa.service.CustomerLoader;

@RestController
public class CustomerController {

    private final CustomerLoader customerLoader;

    public CustomerController(CustomerLoader customerLoader) {
        this.customerLoader = customerLoader;
    }

    @PostMapping(path = "/customer/load",
            //produces = MediaType.TEXT_PLAIN_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity load(@RequestPart(value = "file") MultipartFile file,
                               @RequestAttribute(required = false) String datetime) {
        customerLoader.load(file, datetime);
        return ResponseEntity.status(HttpStatus.OK).body("The uploaded file has been loaded.");
    }
}
