В программе используется API https://blockchain.info/api/blockchain_api,
https://blockchain.info/api/blockchain_wallet_api

Получение баланса по адресу 1JzSZFs2DQke2B3S4pBxaNaMzzVZaG4Cqh:
curl "http://localhost:8000/balance" \
  -X POST \
  -d "1JzSZFs2DQke2B3S4pBxaNaMzzVZaG4Cqh" \
  -H "Content-Type: text/plain" 

Получение транзакций по адресу 1JzSZFs2DQke2B3S4pBxaNaMzzVZaG4Cqh:
curl "http://localhost:8000/transaction" \
  -X POST \
  -d "1JzSZFs2DQke2B3S4pBxaNaMzzVZaG4Cqh" \
  -H "Content-Type: text/plain" 


Для отправки транзакций надо установить утилиту 
$ npm install -g blockchain-wallet-service
Запустить её:
$ nohup blockchain-wallet-service start --port 3000 &



  Отправка транзакции по переводу биткоинов:
curl "http://localhost:8000/payment?api_code=2&identifier=2&password=2&toAddress=2&amount=2&fee=2" \
  -d "1JzSZFs2DQke2B3S4pBxaNaMzzVZaG4Cqh" \
  -H "Content-Type: text/plain" 

Создать докер image:
$ docker build -t copper.co/app:0.0.1 .

Запустить:
$ docker-compose up -f docker-compose-copper-app.yaml
