# WebSocket Endpoint
* [DataListener](#DataListener)

### DataListener
Starts a listener for new data that can be decrypted by the provided account.

Endpoint: <wss://localhost:8550/dataListener>

#### Connect
An **_account_** has to be connected to the websocket to receive data for.

##### Message
* `Object` - **_account_** to connect as.
```js
{
    "identifier": "407d73d8a49eeb85d32cf465507dd71d507100c1",
    "password": "myPassword"
}
```

##### Response
* `String` - Listener handle of the established connection.

```js
ded64f8bfeff4785
```

##### Update
* `String` - **_data identifier_**.

```js
ee8e01eff7acd538e8f6e6deea1a971e1be920ee4ceb4419434315dac04ed736
```

##### Error
If an error occurs the conversation will be closed with a status code and reason phrase.

Next to the pre-defined errors of the WebSocket Protocol specification (RFC 6455), the following implementation-specific error might occur:

| Code | Message | Meaning |
|------|---------|---------|
| 4000 | Invalid Message | The sent message has an invalid format. |
| 4001 | Already Connected | The conversation already had a connected account. |
| 4002 | Invalid Account | Account password is invalid or account does not exist. |

***
