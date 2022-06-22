# Сборка приложения.

runawfe-server/wfe-app> mvn clean package -P spa
Подложить runawfe.ear в wildly и запустить:
* http://localhost:8080/wfe/
* http://localhost:8080/spa/

# Запуск приложения в браузере в режиме отладки.

runawfe-server/wfe-app> mvn clean package
Подложить runawfe.ear в wildly и запустить:
* http://localhost:8080/wfe/

runawfe-server/wfe-spa> npm install (nvm use lts/dubnium, если проблема с версиями)
runawfe-server/wfe-spa> npm run debug

# Альтернативный запуск приложения в режиме production с помощью встроенного http-server из NodeJS.
# URL: http://localhost:8080 (порт может отличаться в зависимость от конфигурации)
>>>npm start
