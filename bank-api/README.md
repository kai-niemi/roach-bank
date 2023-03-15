# Roach Bank API

API description and value objects for clients.

Roach Bank provides two main interfaces:
 
 - A hypermedia API for request/response based interactions.
 - A websocket Streaming API for reactive front-ends, driven via CockroachDB CDC (optional) or synthetic events. 

# Hypermedia API

The Hypermedia API is used to view data, create accounts and generate 
monetary transactions. 

## Resources

- List of top accounts per region (for front-ends)
- Paginated list of accounts
- Paginated list of transactions 
- Submitting monetary transactions 
- Reporting
    - Total account balance 
    - Total transfer turnover (money exchanged between accounts)
- Admin
    - Spring boot monitoring endpoints (actuators)

## Media Types

 - ``application/hal+json`` - main hypermedia type 
 - ``application/vnd.error+json`` - error condition details
 
A client SHOULD send an ``Accept`` header with the requested media type. 

## Link Relations

A HTTP client typically follows the hyperlinks provided by the API to guide through different workflows, 
such as placing a monetary transaction, or browsing through pages of account details. 

As with any REST API, 
following hyperlinks is optional. A client can also bind directly to the resource URI:s with tight coupling 
as a result. The semantics of the endpoints are tied to the link relations rather than the opaque URI:s.

- https://en.wikipedia.org/wiki/Hypertext_Application_Language 
- https://en.wikipedia.org/wiki/HATEOAS

Example of link relations at root level:

- roachbank:account
    - GET - Returns an account creation and listing resource.

      Example:

          curl -i -X GET http://localhost:8090/api/account

- roachbank:transaction
    - GET - Returns a transaction creation and listing resource.

      Example:

          curl -i -X GET http://localhost:8090/api/transfer

- roachbank:reporting
    - GET - Returns a reporting summary resource

      Example:

          curl -i -X GET http://localhost:8090/api/report

Example of link relations under `roachbank:transaction`:

-  roachbank:form
    - GET - Returns a pre-populated transfer request form
    
      Example:
      
          curl -X GET http://localhost:8090/api/transfer/form > form.json
    
    - POST - Places a monetary transaction across at least two accounts. 
        - Body: a transfer request form where the sum of the transaction legs must
         equal zero and no account balance can end up negative.
        - Status Code: 201 for created, 200 if previously observed and de-duplicated.
        
      The POST method is unsafe but idempotent key:ed on _transactionId_.
        
      Example POST request:
    
          curl -d "@form.json" -H "Content-Type:application/json" -X POST http://localhost:8090/api/transfer

### Errors

Errors with 4xx and 5xx status codes are represented with a special response body using 
content type ``application/problem+json``.

Example:

    {
        "status": "BAD_REQUEST",
        "type": "http://en.wikipedia.org/wiki/HTTP_400",
        "detail": "JSON parse error: Cannot deserialize value of type `java.util.UUID` from String \"5d526af4-4dcf-412f-a899-12555b33efc\": UUID has to be represented by standard 36-char representation; nested exception is com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `java.util.UUID` from String \"5d526af4-4dcf-412f-a899-12555b33efc\": UUID has to be represented by standard 36-char representation\n at [Source: (PushbackInputStream); line: 12, column: 18] (through reference chain: org.magickingdom. bank.api.TransferForm[\"transferId\"])",
        "errors": [
            "null"
        ],
        "statusCode": 400
    }

### Suggested Tools

 - Postman for testing HTTP requests (https://www.getpostman.com) 
 - JSONView - A Chrome plugin for colorizing JSON 
 
# Streaming API 

Balance updates can be captured via CDC and sent to Kafka and then subscribed back 
and published as refined events over websockets (the Event API). This eliminates 
the non-atomic dual-write problem. If Kafka is not used, the events are published
via an AOP aspect using deferred dual-writes after transaction completion. 
Streaming events are pushed to websocket clients via STOMP protocol. 

### SockJS Client Example

Client side example of subscribing to the change stream using SockJS + Stomp.
    
    var socket = new SockJS('http://localhost:8090/roach-bank');

    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/account-summary', function (account) {
            var event = JSON.parse(account.body);
        });
    });

