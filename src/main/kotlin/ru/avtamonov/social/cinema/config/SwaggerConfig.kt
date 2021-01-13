package ru.avtamonov.social.cinema.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
 * Конфигурация сваггера
 */
@Configuration
@EnableSwagger2
class SwaggerConfig {

    /**
     * Настройка свагера
     */
    /** Инициализация основного бина для работы сваггера **/
    @Bean
    fun api(): Docket =
            Docket(DocumentationType.SWAGGER_2)
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("ru.avtamonov.social.cinema.controller"))
                    .paths(PathSelectors.any())
                    .build()
                    .apiInfo(getApiInfo())
                    .useDefaultResponseMessages(false)

    /**
     * Описание сервиса
     */
    private fun getApiInfo(): ApiInfo =
            ApiInfo(
                    "Social cinema API",
                    "Система бронирования билетов в социальном кинотеатре",
                    "",
                    "",
                    Contact("", "", ""),
                    "",
                    "", ArrayList())
}
