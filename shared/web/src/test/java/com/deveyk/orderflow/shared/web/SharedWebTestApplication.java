package com.deveyk.orderflow.shared.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * shared-web'in kendi testleri için {@code @WebMvcTest}'in aradığı Spring Boot konfigürasyonu.
 * shared-web bir uygulama değil, kütüphanedir; bu sınıf sadece testte vardır.
 */
@SpringBootApplication
public class SharedWebTestApplication {
}
