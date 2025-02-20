    theme /general
    
    state start
        intent Приветствие
        action
            reply "Привет! Я могу помочь найти объявления о продаже автомобилей. Скажи марку, модель и год выпуска."
    
    state search_state
        intent Поискавто
        action
            $make = nlu.value("Марка")
            $model = nlu.value("Модель")
            $year = nlu.value("Год")
            
            if $make and $model and $year
                $query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f&make=" + $make + "&model=" + $model + "&year=" + $year
                $http.get($query) > response
                
                if response.status == "Failed"
                    reply "Ошибка при получении данных. Попробуйте снова."
                else
                    $cars = response.cars
                    if $cars.size > 0
                        reply "Я нашел " + $cars.size + " объявлений:"
                        foreach $car in $cars
                            reply $car.url
                    else
                        reply "❌ Ничего не найдено 😢 Попробуйте изменить параметры."
            else
                reply "Не смог распознать запрос. Попробуйте сказать, например: 'Найди Toyota Camry 2010'."
    
    
