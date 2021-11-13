package hu.bme.szgbizt.levendula.caffplacc.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HelloServiceTest {

    @Autowired
    private HelloService service;

    @Test
    void helloTest() {
        Assertions.assertEquals("Hello, John", service.hello("John"));
    }

}
