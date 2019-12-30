package com.amtkxa.presentation.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amtkxa.common.util.DateUtil;
import com.amtkxa.service.CustomerLoader;

@RestController
public class CustomerController {

    private final CustomerLoader customerLoader;

    public CustomerController(CustomerLoader customerLoader) {
        this.customerLoader = customerLoader;
    }

    @PostMapping(path = "/customer/load", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity load(@RequestPart(value = "file") MultipartFile file,
                               @RequestParam(required = false) String datetime) {
        customerLoader.load(file, DateUtil.parse(
                Optional.ofNullable(datetime).orElse(DateUtil.now().toString())
        ));
        return ResponseEntity.status(HttpStatus.OK).body("The uploaded file has been loaded.");
    }
}
