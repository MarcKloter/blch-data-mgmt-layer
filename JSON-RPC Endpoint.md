# JSON-RPC API
## Custom Types
Implementation specific data types:

| Type Name             | Base Type |
|-----------------------|-----------|
| **_address_**         | `String` - Sequence of 20 bytes in Hex representation that can be resolved to a public key. |
| **_data identifier_** | `String` -  Sequence of 32 bytes in Hex representation that uniquely identifies data stored in the blockchain. |
| **_account_**         | `Object` <ul><li>`identifier: String` - **_address_** of the account.</li><li>`password: String` - Password to unlock the `address` account. </li></ul> |

## JSON-RPC Endpoint
Default JSON-RPC endpoint: <https://localhost:8550>

## JSON-RPC Methods
* [storeData](#storeData)
* [listData](#listData)
* [getData](#getData)
* [createAccount](#createAccount)

## Error Condition Responses
When a rpc call encounters an error, the response contains the error member `Object`:
* `code: Integer` - A number that indicates the error type that occurred.
* `message: String` - A string providing a short description of the error.
* `data: Object` - (optional) Value that contains additional information about the error.

The pre-defined errors cover:

| Code   | Message | Meaning |
|--------|---------|---------|
| -32700 | Parse error | Invalid JSON was received by the server. An error occurred on the server while parsing the JSON text. |
| -32600 | Invalid request | The JSON sent is not a valid JSON-RPC request object. |
| -32601 | Method not found | The method does not exist / is not available. |
| -32602 | Invalid params | Invalid method parameter(s). |
| -32500 | Internal server error | The server encountered an unexpected condition which prevented it from fulfilling the request. |
| -32501 | Service unavailable | The server is currently unable to handle the request. |
| -32502 | Timeout | The server did not receive a timely response from an auxiliary server required to complete the request. |

The space -32000 to -32099 is reserved for implementation-defined errors, which cover:

| Code   | Message | Meaning |
|--------|---------|---------|
| -32000 | Authentication error | Account password is invalid or account does not exist. |
| -32001 | Not authorized | The referenced data could not be accessed. |

## JSON-RPC API Reference

### storeData
Stores data in the connected blockchain.

#### Parameters
* `data: String` - Data to store.
* `attachments: Array` - (optional) Array of **_data identifiers_** of data to attach to this document.
* `account: Object` - **_account_** to interact as.
* `subjects: Array` - Array of **_addresses_** to authorize. The data will be encrypted using the related public keys. The public key associated with the `account` is implicitly added to the `subjects`.

#### Returns
* `String` - The **_data identifier_** of the stored data.

#### Example
```js
// Request
{
    "jsonrpc": "2.0",
    "method": "storeData",
    "params": {
        "data": "myData",
        "attachments": ["ee8e01eff7acd538e8f6e6deea1a971e1be920ee4ceb4419434315dac04ed736"],
        "account": {
            "identifier": "407d73d8a49eeb85d32cf465507dd71d507100c1",
            "password": "myPassword"
        },
        "subjects": ["616fb1f986a25482dcce030adc591d79d0ffd165", "064b43a92fa5e2aec118e3952b9510ab16"]
    },
    "id": 67
}

// Result
{
    "id": 67,
    "jsonrpc": "2.0",
    "result": ["132e5838bc711f27c455500baee4d8ad809e5d3e617b29d86a7b025904a2e1ed"]
}
```

***

### listData
Lists all identifiers of data that can **directly** be accessed by the provided account.

Direct accessible data are entries where the provided account is either creator or recipient of. Attachments will not be included in the result.

#### Parameters
* `account: Object` - **_account_** to interact as.

#### Returns
* `Array` - Array of **_data identifiers_**.

#### Example
```js
// Request
{
    "jsonrpc": "2.0",
    "method": "listData",
    "params": {
        "account": {
            "identifier": "407d73d8a49eeb85d32cf465507dd71d507100c1",
            "password": "myPassword"
        }
    },
    "id": 68
}

// Result
{
    "id": 68,
    "jsonrpc": "2.0",
    "result": [
        "132e5838bc711f27c455500baee4d8ad809e5d3e617b29d86a7b025904a2e1ed",
        "ee8e01eff7acd538e8f6e6deea1a971e1be920ee4ceb4419434315dac04ed736"
    ]
}
```

***

### listDataChanges
Lists identifiers of data that can **directly** be accessed by the provided account since the last time this method was called.

Direct accessible data are entries where the provided account is either creator or recipient of. Attachments will not be included in the result.

#### Parameters
* `account: Object` - **_account_** to interact as.

#### Returns
* `Array` - Array of **_data identifiers_**.

#### Example
```js
// Request
{
    "jsonrpc": "2.0",
    "method": "listDataChanges",
    "params": {
        "account": {
            "identifier": "407d73d8a49eeb85d32cf465507dd71d507100c1",
            "password": "myPassword"
        }
    },
    "id": 69
}

// Result
{
    "id": 69,
    "jsonrpc": "2.0",
    "result": [
        "132e5838bc711f27c455500baee4d8ad809e5d3e617b29d86a7b025904a2e1ed"
    ]
}
```

***

### getData
Returns data stored in the connected blockchain.

#### Parameters
* `id: String` - **_data identifier_**.
* `account: Object` - **_account_** to interact as.

#### Returns
* `Object` - Previously stored data:
   * `data: String` - The stored data.
   * `attachments: Array` - (optional) Array of **_data identifiers_** of data attached to this document.

#### Example
```js
// Request
{
    "jsonrpc": "2.0",
    "method": "getData",
    "params": {
        "id": "132e5838bc711f27c455500baee4d8ad809e5d3e617b29d86a7b025904a2e1ed",
        "account": {
            "identifier": "407d73d8a49eeb85d32cf465507dd71d507100c1",
            "password": "myPassword"
        }
    },
    "id": 70
}

// Result
{
    "id": 70,
    "jsonrpc": "2.0",
    "result": {
        "data": "myData",
        "attachments": ["ee8e01eff7acd538e8f6e6deea1a971e1be920ee4ceb4419434315dac04ed736"]
    }
}
```

***

### createAccount
Creates a new account to store and receive data as, secured by the given password.

#### Parameters
* `password: String` - Password.

#### Returns
* `String` - The **_address_** of the created account.

#### Example
```js
// Request
{
    "jsonrpc": "2.0",
    "method": "createAccount",
    "params": {
        "password": "myPassword" 
    },
    "id": 71
}

// Result
{
    "id": 71,
    "jsonrpc": "2.0",
    "result": "407d73d8a49eeb85d32cf465507dd71d507100c1"
}
```

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
