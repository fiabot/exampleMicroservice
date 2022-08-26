# Example MicroService 

This project shows how to implement a simple microservice. It simulates an API that would 
deal with accounts and transactions between accounts. Calls to the API modify a local sql database.  

## Running the service 

### Create SQL database 
The service is going to look for a SQL database on port 3306 with name 
AccountService. 

During or after installation of mySql, the user "root" with password "IAmRoot" will 
need to be given access to a mySql connection on port3306.  Once this is complete the database can be created by entering into mySql

```
CREATE DATABASE AccountService; 
```
 
If different login or database info is desired, this can be changed 
in the application.properties file.

JPA (should) create a Transactions and Accounts table automatically, but this can be done manually as well.  

This will only need to be done once. 

###Install Maven dependencies 

Must have Maven installed on you workspace. 

In Eclipse: Run top level project as Maven install 

In command line: Within project directory enter

 ```
 mvn install
 ```
 
### Start server 

Run ExampleServiceApplication as Java application. This takes a couple of seconds to start. 

### Open API on browser 

Two different API interfaces are available: 

REST service on Swagger: located on localhost:8080\swagger-ui\ 

GraphQL: located on localhost:8080\graphiql\  

### Get Authenticated 

Sample accounts are available with the following information: 

| username | password| Authorities|  
|----------|---------|------------|
|admin     | IAmAdmin| user, admin|
|user1     | IAmUser1| user, ACC_1111| 
|user2     | IAmUser2| user, ACC_2222| 


Authentication on REST API: 

* enter login information to the authenticate request under the Auth tag
* copy token from response 
* enter `Bearer <jwtToken>` into the the authenticate form in the upper right of the webpage 

After this all requests will be sent with this token in the header 

Authentication on GraphQl: 

* Get token by entering login information to getAuthToken query, such as 

```
query{
  getAuthToken(username: "user1", password: "IAmUser1")
}

```
* Run query and get the string JWT token  

* In the "request headers" tab at the bottom of screen enter the token in the following format 

```
{"Authorization" : "Bearer <jwtToken>"}
```

Then all queries will be made with this token in the header. 

## Service Overview 

### Models

This service relies on two basic models, Account and Transaction. 

 
**Account**
 
An account represents a bank account and contains the following information: 

* Account number: a string representing an account number 
* Value: the amount of money currently in account 
* Outgoing transactions: transactions that the account sends to another account   
* Incoming transactions: transactions that the account receives from another account 
 
 It should be noted though, that outgoing and incoming transactions are not represented in the SQL database, 
 and instead are logically connected in the source code. 
 
** Transaction**
 
An account represents a money transfer between two accounts and contains the following information: 
 
* fromAccount: the account number of the account sending the transaction 
* toAccount: the account number of the account receiving the transaction 
* value: the monetary value of the transaction 
* status: whether the transaction has been processed 
* id: a unique number identifying the transaction 
 
 
### Functions 
 
The following operations can done for the models 
 
* create new account/transaction: creates and posts a new model to database 
* get all accounts/transactions: gathers all resources 
* get transaction by id: get a transaction given the id number 
* get account by account number: get a transaction by account number 
* get incoming/outgoing transactions: get the transactions associated with an account number. Note this is done with getAccount for graphQL. 

GraphQL only functions: 
* delete transaction/account: deletes a transaction or account
* update account value : changes the value in an account 
* process transaction : process a transaction by removing value from sending account, and adding to recieving account 

 
### Database 

The AccountService database has two table representing the two different models.

Accounts: 
* account_number: a unique text value representing account number 
* value: a float value representing current money available to account 
* id: an int value that auto-increments and is the primary key used to help identify accounts, hidden to user 

Transactions: 

* fromAccount: a text value for the account number of sending account 
* toAccount: a text value for the account number of receiving account 
* value: a float value representing money amount of transaction 
* status: a string representing the current status of transaction 
* id: an into value that  auto-increments and is the primary key used to identify transaction 
 
 ### Authorization 
 Certain functions are available only to certain users. 
 
 ** Admin only functions: ** 
 * Create account 
 * delete account / transaction 
 * get all accounts / transactions 
 * updateAccountValue 
 
 ** User functions ** 
 * getAccountByNumber (must have ACC_accountNumber authority) 
 * getTransactionById (must have ACC_accountNumber authority for either incoming or outgoing account on transaction) 
 * createTransaction (must have ACC_accountNumber authority for fromAccount) 
 * getIncomingTransactions/ getOutgoingTransactions (must have ACC_accountNumber authority) 
 * processTransaction (must have ACC_accountNumber authority for fromAccount) 
 
 Admin users can access these functions for all accounts. 
