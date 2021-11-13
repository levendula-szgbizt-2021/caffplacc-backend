package hu.bme.szgbizt.levendula.caffplacc.controller;

import hu.bme.szgbizt.levendula.caffplacc.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private HelloService service;

    @GetMapping(value = "/{name}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String hello(@PathVariable("name") String name) {
        log.info("Saying hello to " + name);
        return service.hello(name);
    }

}
