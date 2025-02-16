theme /general

state start
    intent приветствие
        - (привет|здравствуй|начать|приветствую|добрый день|добрый вечер|доброе утро)
        Привет! Я помогу найти авто 🚗  
        Введите марку, модель, цену, пробег и другие параметры.

    intent поиск_марки
        - (Найди|Подбери|Покажи) {марка}
        -> search_state
        $session.make = {марка}

    intent поиск_марки_модели
        - (Найди|Подбери|Покажи) {марка} {модель}
        -> search_state
        $session.make = {марка}
        $session.model = {модель}

    intent поиск_по_цене
        - (Найди|Подбери|Покажи) {марка} до {цена} рублей
        -> search_state
        $session.make = {марка}
        $session.max_price = {цена}

    intent поиск_по_цене_и_пробегу
        - (Найди|Покажи|Помоги найти) {марка} {модель} до {цена} рублей с пробегом до {пробег} км
        -> search_state
        $session.make = {марка}
        $session.model = {модель}
        $session.max_price = {цена}
        $session.max_mileage = {пробег}

state search_state
    script
        var query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f";

        if (session.make) {
            query += "&make=" + session.make;
        }
        if (session.model) {
            query += "&model=" + session.model;
        }
        if (session.max_price) {
            query += "&max_price=" + session.max_price;
        }
        if (session.max_mileage) {
            query += "&max_mileage=" + session.max_mileage;
        }

        var response = http.get(query);

        if (response.status == "Failed") {
            say("❌ Ошибка API: " + response.msg);
            say("Попробуйте позже.");
            goto("start");
        }

        var cars = response.cars;
        if (cars.length > 0) {
            say("✅ Найдено несколько вариантов:");
            for (var i = 0; i < Math.min(3, cars.length); i++) {
                var car = cars[i];
                say("➤ **" + car.make + " " + car.model + " " + car.year + "**");
                say("💰 Цена: **" + car.price + " ₽**");
                say("🚗 Пробег: **" + car.mileage + " км**");
                say("⛽ Топливо: **" + car.fuel_type + "**");
                say("📍 Город: **" + car.region + "**");
                say("🔗 [Ссылка на объявление](" + car.url + ")");
            }
            say("🔍 Хотите уточнить поиск?");
        } else {
            say("❌ Ничего не найдено 😢 Попробуйте изменить параметры.");
            goto("start");
        }
    end
