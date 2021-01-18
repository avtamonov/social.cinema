package ru.avtamonov.social.cinema.controller

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.avtamonov.social.cinema.controller.ApiInfoController


@RunWith(SpringRunner::class)
@WebMvcTest(ApiInfoController::class)
class ApiInfoControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private val resp = listOf("/swagger-ui.html")

    @Test
    fun `should a get Info`() {
        val response = mockMvc.perform(get("/v1/")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection).andReturn()
                .response.getHeaderValues("Location")
        assertThat(response).isEqualTo(resp)
    }
}