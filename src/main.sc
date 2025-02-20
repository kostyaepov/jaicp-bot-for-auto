theme /general

// Стартовое состояние
state start
    intent приветствие
        - Привет! Я помогу найти авто 🚗  
        - Введите марку, модель, цену, пробег и другие параметры.

    intent поиск_авто
        $session.make = nlu.entities.марка[0]
        $session.model = nlu.entities.модель[0]
        $session.max_price = nlu.entities.параметры[цена]
        $session.max_mileage = nlu.entities.параметры[пробег]
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
        -> ошибка_API

    $cars = response.cars
    if $cars.size > 0
        - ✅ Найдено несколько вариантов:
        for $car in $cars[0:3]
            - **$car.make $car.model $car.year**
            - 💰 Цена: **$car.price ₽**
            - 🚗 Пробег: **$car.mileage км**
            - ⛽ Топливо: **$car.fuel_type**
            - 📍 Город: **$car.region**
            - 🔗 [Ссылка на объявление]($car.url)
        - 🔍 Хотите уточнить поиск?
        -> уточнение_поиска
    else
        - ❌ Ничего не найдено 😢 Попробуйте изменить параметры.
        -> start

// Ошибка API
state ошибка_API
    intent ошибка_API
        - ❌ Ошибка API: Попробуйте позже.
        -> start

// Уточнение поиска
state уточнение_поиска
    intent уточнение_поиска
        - Введите новые параметры для поиска
        -> start
