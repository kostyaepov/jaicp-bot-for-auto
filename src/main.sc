start
    // Приветствие
    + (привет|здравствуй|начать|приветствую|добрый день|добрый вечер|доброе утро)
        Привет! Я помогу найти авто 🚗  
        Введите марку, модель, цену, пробег и другие параметры.

    // Поиск только по марке (разные фразы)
    + (Найди|Помоги мне найти|Покажи|Подбери) {марка}
        $session.make = {марка}
        -> search_state

    // Поиск по марке и модели
    + (Найди|Подбери|Покажи) {марка} {модель}
        $session.make = {марка}
        $session.model = {модель}
        -> search_state

    // Поиск по марке и цене
    + (Найди|Подбери|Покажи) {марка} до {цена} рублей
        $session.make = {марка}
        $session.max_price = {цена}
        -> search_state

    // Поиск по нескольким параметрам
    + (Найди|Покажи|Помоги найти) {марка} {модель} до {цена} рублей с пробегом до {пробег} км
        $session.make = {марка}
        $session.model = {модель}
        $session.max_price = {цена}
        $session.max_mileage = {пробег}
        -> search_state

search_state
    // Формируем URL запроса к API crwl.ru
    $query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f"

    if $session.make
        $query += "&make=" + $session.make
    if $session.model
        $query += "&model=" + $session.model
    if $session.max_price
        $query += "&max_price=" + $session.max_price
    if $session.max_mileage
        $query += "&max_mileage=" + $session.max_mileage

    // Отправляем API-запрос
    $http.get($query) > response

    // Проверяем, есть ли ошибка в ответе API
    if response.status == "Failed"
        ❌ Ошибка: API-ключ недействителен или заблокирован.
        Проверьте ключ или попробуйте снова позже.
        -> start
    else
        $cars = response.cars

        // Если объявления найдены
        if $cars.size > 0
            ✅ Найдено несколько вариантов:
            for $car in $cars[0:3] // Показываем 3 объявления
                ➤ **$car.make $car.model $car.year**
                💰 Цена: **$car.price ₽**
                🚗 Пробег: **$car.mileage км**
                ⛽ Топливо: **$car.fuel_type**
                📍 Город: **$car.region**
                🔗 [Ссылка на объявление]($car.url)
            🔍 Хотите уточнить поиск?
        else
            ❌ Ничего не нашел 😢 Попробуйте изменить параметры поиска!
