package org.sample.spring.rest.api.service;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void デフォルトの挨拶を返す() throws Exception {
        mockMvc.perform(get("/greeting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void nameパラメータを指定した挨拶を返す() throws Exception {
        mockMvc.perform(get("/greeting").param("name", "Claude"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Claude!"));
    }

    @Test
    void リクエストごとにidがインクリメントされる() throws Exception {
        MvcResult first = mockMvc.perform(get("/greeting"))
                .andExpect(status().isOk()).andReturn();
        MvcResult second = mockMvc.perform(get("/greeting"))
                .andExpect(status().isOk()).andReturn();

        long id1 = JsonPath.parse(first.getResponse().getContentAsString()).read("$.id", Long.class);
        long id2 = JsonPath.parse(second.getResponse().getContentAsString()).read("$.id", Long.class);
        assertThat(id2).isGreaterThan(id1);
    }
}
