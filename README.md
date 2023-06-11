# Websocket-server

Websocket-server является Java приложением для генерации BigInteger и отправки этого результата по протоколу WebSocket.


## Task Description
Написать на Java сервер который является websocket сервером
при запросе на сервер, должен возвращаться ответ в виде JSON - рандомное число BigInteger
сервер не должен позволяет с одного IP адреса устанавливать более 1 соединения
сервер должен гарантировать что число всегда будет уникальное, появившись один раз - больше не должно появляться

не использовать фреймворки типа Spring, RxJava и пр

описать в readme как запустить и использовать сервер

unit тесты и не docker - не обязательно, если в коде будут комментарии

## Usage

```sh
mvn clean package 
```
Запустить приложение с кастомной реализацией перехода на протокол websocket
```sh
java -jar ./target/websocket-server-1.0-SNAPSHOT-jar-with-dependencies.jar custom-handshake
```
Запустить приложение с WebSocketServerProtocolHandler
```sh
java -jar ./target/websocket-server-1.0-SNAPSHOT-jar-with-dependencies.jar default-handshake
```