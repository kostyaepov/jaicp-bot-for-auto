// Указываем тему перед состояниями
theme /general

// Стартовое состояние бота
state start
    intent приветствие
        Привет! Я помогу найти авто 🚗  
        Введите марку, модель, цену, пробег и другие параметры.

    intent поиск_марки (Найди|Подбери|Покажи) {марка}
        $session.make = {марка}
        -> search_state

    intent поиск_модели (Найди|Подбери|Покажи) {марка} {модель}
        $session.make = {марка}
        $session.model = {модель}
        -> search_state

    intent поиск_по_цене (Найди|Подбери|Покажи) {марка} до {цена} рублей
        $session.make = {марка}
        $session.max_price = {цена}
        -> search_state

    intent поиск_с_пробегом (Найди|Покажи|Помоги найти) {марка} {модель} до {цена} рублей с пробегом до {пробег} км
        $session.make = {марка}
        $session.model = {модель}
        $session.max_price = {цена}
        $session.max_mileage = {пробег}
        -> search_state

// Состояние поиска авто
state search_state
    $query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f"

    if $session.make
        $query += "&make=" + $session.make
    if $session.model
        $query += "&model=" + $session.model
    if $session.max_price
        $query += "&max_price=" + $session.max_price
    if $session.max_mileage
        $query += "&max_mileage=" + $session.max_mileage

    $http.get($query) > response

    if response.status == "Failed"
        ❌ Ошибка API: $response.msg
        Попробуйте позже.
        -> start

    $cars = response.cars
    if $cars.size > 0
        ✅ Найдено несколько вариантов:
        for $car in $cars[0:3]
            ➤ **$car.make $car.model $car.year**
            💰 Цена: **$car.price ₽**
            🚗 Пробег: **$car.mileage км**
            ⛽ Топливо: **$car.fuel_type**
            📍 Город: **$car.region**
            🔗 [Ссылка на объявление]($car.url)
        🔍 Хотите уточнить поиск?
    else
        ❌ Ничего не найдено 😢 Попробуйте изменить параметры.
        -> start
