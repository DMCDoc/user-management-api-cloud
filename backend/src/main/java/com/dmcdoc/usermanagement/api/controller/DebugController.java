package com.dmcdoc.usermanagement.api.controller;


import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @PostMapping("/echo")
    public String echo(@RequestBody String body) {
        System.out.println("Received: " + body);
        return "Echo: " + body;
    }

    @PostMapping("/simple-login")
    public Map<String, String> simpleLogin(@RequestBody Map<String, String> request) {
        System.out.println("Login attempt: " + request);
        return Map.of("status", "success", "message", "Debug endpoint works");
    }
}