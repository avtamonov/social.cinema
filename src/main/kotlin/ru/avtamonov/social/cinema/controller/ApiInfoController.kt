package ru.avtamonov.social.cinema.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import springfox.documentation.annotations.ApiIgnore

/**
 * Контроллер редиректа на сваггер
 */
@RequestMapping(path = ["v1"], produces = ["application/json; charset=utf-8"])
@Controller
@CrossOrigin
@ApiIgnore
class ApiInfoController {

    /**
     * Редирект на страницу сваггера с /v1/
     */
    @GetMapping
    fun getInfo() =
            try {
                "redirect:/swagger-ui.html"
            } catch (ex: Exception) {
                ex.message
            }
}