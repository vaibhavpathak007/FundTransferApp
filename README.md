
## Fund transfer App
### Introduction
This is a simple fund transfer backend app where we can add accounts, retrieve account information, and perform amount transfers with overdraft safety and deadlock safety.

### Improvements Made for Production Readiness

1. Added log statements to aid in debugging the application.
2. Introduced a constants file.
3. Added test cases to detect early deployment failure

### API
Default port is 18080

 - Get Account details
 ```
 curl --location 'http://localhost:18080/v1/accounts/test-2'
```
 - Add Account
 ```
 curl --location 'http://localhost:18080/v1/accounts' \
--header 'Content-Type: application/json' \
--data  '{
"accountId": "test-2",
"balance": 100
}'
```
- Transfer Amount
```
curl --location 'http://localhost:18080/v1/accounts/transfer' \
--header 'Content-Type: application/json' \
--data '{
    "accountFromId": "test-2",
    "accountToId": "test-1",
    "transferAmount": 100
}'
```
