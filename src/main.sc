// Стартовое состояние бота
state: start
    intent: (привет|здравствуй|начать|приветствую|добрый день|добрый вечер|доброе утро)
        - Привет! Я помогу найти авто 🚗  
        - Введите марку, модель, цену, пробег и другие параметры.

    intent: (Найди|Подбери|Покажи) {марка}
        $session.make = "{марка}"
        -> search_state

    intent: (Найди|Подбери|Покажи) {марка} {модель}
        $session.make = "{марка}"
        $session.model = "{модель}"
        -> search_state

    intent: (Найди|Подбери|Покажи) {марка} до {цена} рублей
        $session.make = "{марка}"
        $session.max_price = "{цена}"
        -> search_state

    intent: (Найди|Покажи|Помоги найти) {марка} {модель} до {цена} рублей с пробегом до {пробег} км
        $session.make = "{марка}"
        $session.model = "{модель}"
        $session.max_price = "{цена}"
        $session.max_mileage = "{пробег}"
        -> search_state

// Состояние поиска авто
state: search_state
    script:
        $query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f"
        
        if $session.make:
            $query += "&make=" + $session.make
        if $session.model:
            $query += "&model=" + $session.model
        if $session.max_price:
            $query += "&max_price=" + $session.max_price
        if $session.max_mileage:
            $query += "&max_mileage=" + $session.max_mileage

        $http.get($query) > response

        if response.status == "Failed":
            say "❌ Ошибка API: " + response.msg
            say "Попробуйте позже."
            goto start

        $cars = response.cars
        if $cars.size > 0:
            say "✅ Найдено несколько вариантов:"
            for $car in $cars[0:3]:
                say "➤ **" + $car.make + " " + $car.model + " " + $car.year + "**"
                say "💰 Цена: **" + $car.price + " ₽**"
                say "🚗 Пробег: **" + $car.mileage + " км**"
                say "⛽ Топливо: **" + $car.fuel_type + "**"
                say "📍 Город: **" + $car.region + "**"
                say "🔗 [Ссылка на объявление](" + $car.url + ")"
            say "🔍 Хотите уточнить поиск?"
        else:
            say "❌ Ничего не найдено 😢 Попробуйте изменить параметры."
            goto start
