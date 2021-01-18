Система бронирования билетов в социальном кинотеатре

Для документирования REST API использовался Swagger. Переход на его страницу http://localhost:8080/swagger-ui.html
Порт по умолчанию 8080
Описание методов контроллера реализовано в сваггере.

В рамках реализации есть условность, что администратор только создает, удаляет, переносит сеансы и включает/отключает скидку,
а клиенты только смотрят историю посещённых сеансов, список всех сеансов, бронируют билеты и отменяют бронь. Клиенты отличаются login'ом

В пакете test реализованы модульные тесты на SessionServiceImpl.kt, перенаправление сваггером и CinemaSessionController.kt

Я предполагаю, что в решении должны были использоваться Coroutine, но как его использовать в решении я, так и не придумал.

Конфигурация настроек приложения:

"social-discount-1" - скидка для первой категории

"social-discount-2" - скидка для второй категории

"social-discount-3" - скидка для третьей категории

"timeZone" - часовой пояс для системного времени

"min-profit" - минимальная прибыль в процентах, где 100% прибыль при полной посадке обычных билетов. [0; 100]

"delay-time-for-standard-category" - время, когда будут доступны билеты для стандартной категории. 
Работает по формуле: из даты начала сеанса вычесть значение в настройке. Указывается в минутах

"payback-time" - время, за которое будет проверка занятости зала социальными категориями. 
Если их будет меньше "min-profit", то откроется продажа стандартным покупателям.

При выключенных скидках, бронировать билеты стандартная категория может сразу.

Сервис позволяет клиенту как минимум:
1. Просмотреть список сеансов. GET /v1/session/
2. Забронировать билет на сеанс. POST /v1/session/reserve
3. Просмотреть информацию о забронированных местах. GET /v1/session/reserved
4. Отменить бронирование. PUT /v1/session/reserve
5. Просмотреть историю посещенных сеансов. GET /v1/session/history 

Сервис позволяет администратору как минимум:
1. Добавить сеанс. POST /v1/session
2. Перенести сеанс. POST /v1/session/transfer
3. Отменить сеанс. DELETE /v1/session{id}

Детальную информацию по телу запроса можно посмотреть в сваггере.