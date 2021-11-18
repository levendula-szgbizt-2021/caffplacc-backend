package hu.bme.szgbizt.levendula.caffplacc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class CaffplaccApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaffplaccApplication.class, args);
        log.info("Application has started successfully!");
    }

}
