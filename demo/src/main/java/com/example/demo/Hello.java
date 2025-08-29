package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class Hello {
    /**
     * This method returns a greeting message.
     *
     * @return A string containing the greeting message.
     */
    public void sayHello() {
        System.out.println("Hello, World!");
    }
}