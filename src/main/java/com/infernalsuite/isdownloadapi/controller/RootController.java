package com.infernalsuite.isdownloadapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@Controller
public class RootController {
    @GetMapping({
            "/",
            "/docs",
    })
    public ResponseEntity<?> redirectToDocs() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("docs/"))
                .build();
    }
}
