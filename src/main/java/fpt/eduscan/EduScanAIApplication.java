package fpt.eduscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EduScanAIApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduScanAIApplication.class, args);
        System.out.println("=================================");
        System.out.println("EduScan API is running!");
        System.out.println("Server: http://localhost:8080");
        System.out.println("Health Check: http://localhost:8080/api/ocr/health");
        System.out.println("=================================");
    }
}