package com.kyan.mortgagematcher.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class PingController {
    public record PingResponse(String message) {
    }

    @GetMapping("/ping")

    public PingResponse getMethodName() {

        return new PingResponse("Hello from Mortgage Matcher.");
    }

}
