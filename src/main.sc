// Стартовое состояние бота
state start
    intent /^(привет|здравствуй|начать|приветствую|добрый день|добрый вечер|доброе утро)$/i
        - Привет! Я помогу найти авто 🚗  
        - Введите марку, модель, цену, пробег и другие параметры.

    intent /^(найди|подбери|покажи) (?<make>\w+)$/i
        $session.make = "{make}"
        -> search_state

    intent /^(найди|подбери|покажи) (?<make>\w+) (?<model>\w+)$/i
        $session.make = "{make}"
        $session.model = "{model}"
        -> search_state

    intent /^(найди|покажи|подбери) (?<make>\w+) до (?<price>\d+) рублей$/i
        $session.make = "{make}"
        $session.max_price = "{price}"
        -> search_state

    intent /^(найди|покажи|помоги найти) (?<make>\w+) (?<model>\w+) до (?<price>\d+) рублей с пробегом до (?<mileage>\d+) км$/i
        $session.make = "{make}"
        $session.model = "{model}"
        $session.max_price = "{price}"
        $session.max_mileage = "{mileage}"
        -> search_state

// Состояние поиска авто
state search_state
    script
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
