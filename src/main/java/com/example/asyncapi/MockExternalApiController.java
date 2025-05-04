//package com.example.asyncapi;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/mock-api")
//public class MockExternalApiController {
//
//    @PostMapping("/post")
//    public ResponseEntity<Map<String, Object>> mockPost(@RequestBody(required = false) Map<String, Object> body) throws InterruptedException {
//        try {
//            Thread.sleep(100); // Simulate 100ms processing delay
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // restore interrupt status
//        }
//        Map<String, Object> response = new HashMap<>();
//        response.put("url", "http://localhost:8083/mock-api/post");
//        response.put("data", body);
//        response.put("message", "Mocked response success");
//        return ResponseEntity.ok(response);
//        // simulate 100ms delay
//
//    }
//}
