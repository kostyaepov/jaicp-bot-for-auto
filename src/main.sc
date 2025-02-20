theme /general

state start
    intent Greeting
        reactions
            telegram.say "Привет! Я помогу найти машину. Просто скажите марку, модель и год выпуска."

    intent SearchCar
        reactions
            $make = "{{Марка}}"
            $model = "{{Модель}}"
            $year = "{{Год}}"

            $query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f&make=" + $make + "&model=" + $model + "&year=" + $year

            $http.get($query) > response

            if response.status == "Failed"
                telegram.say "Ошибка при получении данных. Попробуйте снова."
                exit

            $cars = response.cars

            if $cars.size > 0
                telegram.say "Я нашел " + $cars.size + " объявлений для вас!"
                for $car in $cars
                    telegram.say "🚗 " + $car.title + "\n🔗 " + $car.url
            else
                telegram.say "❌ Ничего не найдено. Попробуйте изменить параметры."

    fallback
        reactions
            telegram.say "Извините, я не понял запрос. Попробуйте сказать марку, модель и год авто."
