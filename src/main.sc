# Настройки проекта
theme: /general

# Сценарий диалога
script

    # Начало беседы
    step
        message Привет! Я помогу найти авто 🚗. Введите марку, модель, цену, пробег и другие параметры.
        next ask_user

    # Ожидание ввода пользователя
    step ask_user
        input
        next check_intent

    # Обработка интентов
    step check_intent
        condition
            value text
            equals (привет|здравствуй|начать|приветствую|добрый день|добрый вечер|доброе утро)
            next greet_user

        condition
            value text
            matches (Найти|Подобрать|Показать) {марка}
            next find_car_by_make

        condition
            value text
            matches (Найти|Подобрать|Показать) {марка} {модель}
            next find_car_by_make_and_model

        condition
            value text
            matches (Найти|Подобрать|Показать) {марка} до {цена} рублей
            next find_car_by_make_and_price

        condition
            value text
            matches (Найти|Подобрать|Помочь найти) {марка} {модель} до {цена} рублей с пробегом до {пробег} км
            next find_car_by_all_params

        condition
            next unknown_command

    # Приветствие пользователя
    step greet_user
        message Привет! Готов помочь вам с поиском автомобиля.
        next ask_user

    # Поиск машины по марке
    step find_car_by_make
        script_call find_car(make={text.match(1)})
        next show_results

    # Поиск машины по марке и модели
    step find_car_by_make_and_model
        script_call find_car(make={text.match(1)}, model={text.match(2)})
        next show_results

    # Поиск машины по марке и цене
    step find_car_by_make_and_price
        script_call find_car(make={text.match(1)}, max_price={text.match(2)})
        next show_results

    # Поиск машины по всем параметрам
    step find_car_by_all_params
        script_call find_car(make={text.match(1)}, model={text.match(2)}, max_price={text.match(3)}, max_mileage={text.match(4)})
        next show_results

    # Неизвестная команда
    step unknown_command
        message Не совсем понял ваш запрос. Пожалуйста, уточните критерии поиска.
        next ask_user

    # Показ результатов поиска
    step show_results
        condition
            value result
            equals []
            next no_cars_found

        condition
            next display_cars

    # Отображение информации о машинах
    step display_cars
        message ✅ Найдено несколько вариантов:
        foreach car in result
            message |
                ➤ **{car.make} {car.model} {car.year}**
                💰 Цена: **{car.price} ₽**
                🚗 Пробег: **{car.mileage} км**
                ⛽ Топливо: **{car.fuel_type}**
                📍 Город: **{car.region}**
                🔗 [Ссылка на объявление]({car.url})
        message 🔍 Хотите уточнить поиск?
        next ask_user

    # Если машины не найдены
    step no_cars_found
        message ❌ Ничего не найдено 😢 Попробуйте изменить параметры.
        next ask_user

end_script

# Функция поиска машины
function find_car(make, model=null, max_price=null, max_mileage=null)
    # Формируем URL запроса к API
    set query = "https://crwl.ru/api/rest/latest/get_ads/?api_key=4309e95538b30c8ae3998ce980df9a1f"
    
    # Добавляем параметры поиска
    if make != null
        append query &= "&make=" & make
        
    if model != null
        append query &= "&model=" & model
        
    if max_price != null
        append query &= "&max_price=" & max_price
        
    if max_mileage != null
        append query &= "&max_mileage=" & max_mileage

    # Выполняем HTTP запрос
    http get query > response

    # Проверяем статус ответа
    if response.status == "Failed"
        return []

    # Возвращаем список машин
    return response.cars
end_function