package hu.bme.szgbizt.levendula.caffplacc.controller;

import hu.bme.szgbizt.levendula.caffplacc.service.HelloService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HelloService service;

    @Test
    void helloTest() throws Exception {
        BDDMockito.given(service.hello("John")).willReturn("Hello, John");

        mvc.perform(MockMvcRequestBuilders.get("/hello/John"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Hello, John"));

        Mockito.verify(service, Mockito.times(1)).hello("John");
    }

}
