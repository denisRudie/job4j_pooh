### Pooh JMS
[![Build Status](https://travis-ci.com/denisRudie/job4j_pooh.svg?branch=master)](https://travis-ci.com/denisRudie/job4j_pooh)

### О проекте
В этом проекте попытался сделал собственную, упрощенную реализацию Rabbit MQ.
Реализованы механизмы topic и queue
. Для имитации отправки/получения сообщений используется ввод из консоли. При подключении нового клиентского сокета:
* над ним создается обертка, позволяющая отправлять/читать в топики/очереди.
* эта обертка добавляется в список подключенных сокетов.
* для запросов на чтение/отправку создаются новые потоки с помощью ThreadPool.
* запросы на чтение реализованы через Future.
#### Topic
* поставщик публикует сообщение с указанием топика.
* все потребители, подписанные на топик, получают это сообщение. Реализовано через создание локальных копий очереди сообщений для всех потребителей.
#### Queue
* поставщик публикует сообщение с указанием очереди.
* как только первый потребитель прочитал сообщение - оно удаляется из очереди (каждое уникальное сообщение может быть прочитано только 1 раз).
### Technologies
* Java 14 (Core, Concurrent, ThreadPool, IO).
* Junit.