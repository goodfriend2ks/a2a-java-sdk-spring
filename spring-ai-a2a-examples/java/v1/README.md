## Some cURL examples

1. Agent Card for Order Agent
```bash
curl --location 'http://localhost:8081/order/.well-known/agent-card.json' \
--header 'Content-Type: application/json' 
```

2. Agent Card for Seller Agent
```bash
curl --location 'http://localhost:8082/seller/.well-known/agent-card.json' \
--header 'Content-Type: application/json' 
```

3. Send user message to Host Agent
```bash
curl --location 'http://localhost:8080/host/chat' \
--header 'Content-Type: application/json' \
--data 'Get me all B2B orders and also I need the seller information for these orders' 
```

4. Send text message to Order Agent
```bash
curl --location 'http://localhost:8081/order/messages/user-message:send' \
--header 'Content-Type: text/plain' \
--header 'Accept: application/json' \
--data 'Show me all B2B orders' 
```

5. Send JSONRPC message to Order Agent
```bash
curl --location 'http://localhost:8081/order' \
--header 'Content-Type: application/json' \
--header 'Accept: text/event-stream' \
--data '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "SendStreamingMessage",
    "params": {
        "message": {
            "role": "ROLE_USER",
            "parts": [
                {
                    "text": "Show me all B2B orders"
                }
            ],
            "messageId": "376ba46f-f4e9-47a1-a4d1-75f54f9ca13e",
            "contextId": null,
            "taskId": null,
            "referenceTaskIds": null,
            "metadata": null,
            "extensions": null
        },
        "configuration": null,
        "metadata": null,
        "tenant": ""
    }
}' 
```
