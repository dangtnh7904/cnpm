package com.nhom33.quanlychungcu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Class cho hệ thống Quản lý Chung cư
 * Nhóm 33 - CNPM
 */
@SpringBootApplication
public class QuanLyChungCuApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanLyChungCuApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Hệ thống Quản lý Chung cư đã khởi động!");
        System.out.println("Server: http://localhost:8080");
        System.out.println("API Base: http://localhost:8080/api");
        System.out.println("=================================================");
    }
}
