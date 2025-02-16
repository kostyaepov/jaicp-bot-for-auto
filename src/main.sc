theme: general

---

state: start
  action:
    say: Привет! Я помогу найти авто 🚗
    say: Введите марку, модель, цену, пробег и другие параметры.
  next: wait_for_input

---

state: wait_for_input
  action:
    listen: true
  next: process_intent

---

state: process_intent
  action:
    switch: on_intent
      cases:
        # Intent: приветствие
        - case: (привет|здравствуй|начать|приветствую|добрый день|добрый вечер|доброе утро)
          do:
            say: Привет! Готов помочь вам с поиском автомобиля.
          next: wait_for_input

        # Intent: Найти автомобиль по марке
        - case: (Найти|Подобрать|Показать) {марка}
          do:
            set:
              session.make: {марка}
          next: search_state

        # Intent: Найти автомобиль по марке и модели
        - case: (Найти|Подобрать|Показать) {марка} {модель}
          do:
            set:
              session.make: {марка}
              session.model: {модель}
          next: search_state

        # Intent: Найти автомобиль по марке и цене
        - case: (Найти|Подобрать|Показать) {марка} до {цена} рублей
          do:
            set:
              session.make: {марка}
              session.max_price: {цена}
          next: search_state

        # Intent: Найти автомобиль по марке, модели, цене и пробегу
        - case: (Найти|Подобрать|Помочь найти) {марка} {модель} до {цена} рублей с пробегом до {пробег} км
          do:
            set:
              session.make: {марка}
              session.model: {модель}
              session.max_price: {цена}
              session.max_mileage: {пробег}
          next: search_state

        # Default case
        - default:
          do:
            say: Не совсем понял ваш запрос. Пожалуйста, уточните критерии поиска.
          next: wait_for_input

---

state: search_state
  action:
    http:
      url: https://crwl.ru/api/rest/latest/get_ads/
      params:
        api_key: 4309e95538b30c8ae3998ce980df9a1f
        make: ${session.make}
        model: ${session.model}
        max_price: ${session.max_price}
        max_mileage: ${session.max_mileage}
      method: GET
    save: response

  next: process_response

---

state: process_response
  action:
    switch: on_var
      var: response.status
      cases:
        - case: Failed
          do:
            say: ❌ Ошибка API: {response.msg}. Попробуйте позже.
          next: start

        - case: Success
          do:
            say: ✅ Найдено несколько вариантов:
            for_each:
              item: car
              array: ${response.cars}
              limit: 3
              do:
                say: |
                  ➤ **${car.make} ${car.model} ${car.year}**
                  💰 Цена: **${car.price} ₽**
                  🚗 Пробег: **${car.mileage} км**
                  ⛽ Топливо: **${car.fuel_type}**
                  📍 Город: **${car.region}**
                  🔗 [Ссылка на объявление](${car.url})
            say: 🔍 Хотите уточнить поиск?
          next: wait_for_input

        - default:
          do:
            say: ❌ Ничего не найдено 😢 Попробуйте изменить параметры.
          next: start