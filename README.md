# blch-data-mgmt-layer

## Getting Started
### Building a JAR
The `mvn package` phase of the `blch-data-mgmt-layer` root project will build an executable fat JAR into `blch-data-mgmt-layer/target/`. Next to it, there will also be a copy of the signed Bouncy Castle Provider JAR that is required to reside next to the blch-data-mgmt-layer JAR.  

The JAR can be executed to [start the JSON-RPC and WebSocket Endpoint](#JSON-RPC-and-WebSocket-Endpoint) or added to a Java project as external JAR.

### Parity
This proof of concept is built to interact with [Parity Ethereum](https://www.parity.io/ethereum/) and requires the `eth` and `personal` modules.

To set up a local environment, the following command can be used:

```
parity --chain dev --jsonrpc-apis=eth,personal --jsonrpc-cors=all
```

### Setting up the EventStorage smart contract
To store data on the connected ethereum blockchain, the address of a deployed version of the [EventStorage smart contract] is required.

It can be deployed for example by using [Remix] or through the `bdml.blockchain/Setup` class which uses [personal_sendtransaction](https://wiki.parity.io/JSONRPC-personal-module#personal_sendtransaction):

```java
String contractAddress = Setup.deploySmartContract(url, fromAddress, password);
```

Depending on the connected blockchain type, this will require the `fromAdress` account to hold enough GAS.

For the dev chain, a quick way to create an account is by either using the [personal_newAccount](https://wiki.parity.io/JSONRPC-personal-module#personal_newaccount) method of the running JSON-RPC interface (default: http://localhost:8545):

```js
{
	"method": "personal_newAccount",
	"params": ["<password>"],
	"id": 1,
	"jsonrpc": "2.0"
}
```

Or by calling the same method programmatically using [web3j](https://github.com/web3j/web3j) which is deployed within the fat JAR:

```java
String password = "<password>";
Admin web3j = Admin.build(new HttpService("http://localhost:8545"));
NewAccountIdentifier accountIdentifier = web3j.personalNewAccount(password).send();
String fromAddress = accountIdentifier.getAccountId();
```

After deploying the contract, the 20 bytes address must be configured for the blch-data-mgmt-layer to use by providing an `application.properties` file:

```
bdml.blockchain.contract.address=0xc79bd4214487c26756d7bef8f3d4a638ea021ba2
```

This file can be saved in the current working directory (eg. next to the JAR if it is being executed) or in the classpath (eg. your `resources` directory).

After configuring the contract address within the `application.properties` file, the blch-data-mgmt-layer is ready to be used.

### JSON-RPC and WebSocket Endpoint
To run the JSON-RPC and WebSocket Endpoint a keystore file specifying key material for SSL is required.
For development a keypair can be generated using the Java Keytool: 
```
keytool -genkey -keyalg RSA -keysize 2048 -keystore keystore.jks
```

A relative path to this .jks file must be supplied to the jar using the `--keystore` argument along with the `--password` to retrieve the keys:

```
java -jar blch-data-mgmt-layer-1.0.0.jar --keystore keystore.jks --password <password>
```

**Specification**: [JSON-RPC and WebSocket Endpoint](JSON-RPC-Endpoint.md)

Development Note: While using self-signed certificates, remember to adjust SSL verification. 

### Running the JSON-RPC Endpoint within an IDE
To run the JSON-RPC Endpoint in an IDE, the `bdml.core/Starter` class requires the parameters from above as program arguments (in this example the `.jks` file is located in the resources directory, the path is relative to the `blch-data-mgmt-layer` project directory):

```
-k bdml.core/src/main/resources/keystore.jks -p <password>
```

## Configuration
The bdml-data-mgmt-layer can be configured by providing an `application.properties`. This file can be placed in the current working directory, which would be next to the executable JAR or in the root directory of your Java project, or on the classpath.

The configuration will be prioritized in the following order: `application.properties` in the current working directory > `application.properties` on the classpath > [default configuration](bdml.core/src/main/resources/default.application.properties)

The following properties can be set:
| Property | Default | Description |
|----------|---------|-------------|
| `bdml.blockchain.contract.address` | | This property is **mandatory** for the Core to operate and will cause an Exception of `getInstance()` if not specified. It sets the address under which the [EventStorage smart contract] is deployed which is mandatory to store data with the given [Blockchain interface](bdml.services/src/main/java/bdml/services/Blockchain.java) implementation (bdml.blockchain). |
| `bdml.output.directory` | `bdml-data` | This is the directory in which the blch-data-mgmt-layer will store data used for operation (cache databases, persisted maps, ...). |
| `bdml.cache.fallback.block` | `0` | This is the block at which the application will start looking for data within the blockchain. If a cache database was deleted, this is where the cache will fall back to. It makes sense to set this property to the block at which the application was deployed (for example the block the contract transaction is located in), as there will not be any data relevant to this application before that. |
| `bdml.blockchain.parity.jsonrpc.uri` | `http://localhost:8545` | This is the URI of the parity JSON-RPC endpoint. |
| `bdml.blockchain.parity.websocket.uri` | `ws://localhost:8546` | This is the URI of the parity WebSocket endpoint. |

## Using the Core service
All available methods of the Core service are defined in the [bdml.services/Core interface](bdml.services/src/main/java/bdml/services/api/Core.java).

Getting the core instance:

```java
Core core = CoreService.getInstance();
```

Creating an account:
```java
String accountID = core.createAccount(password);

// account object required for further methods
Account account = new Account(accountID, password);
```

Storing data:
```java
String dataID = core.storeData("my data string", account);
```

Querying the stored data:
```java
Data data = core.getData(dataID, account);
```

## Cleanup
### Parity
The `db kill` command can be used to clean the database of a given chain:
```
parity --chain dev db kill
```

## Protocol Buffers
After modifying `.proto` files, their Java source files need to be regenerated using the Protocol Buffers Compiler for the changes to take effect.

Instructions: [Compiling Your Protocol Buffers](https://developers.google.com/protocol-buffers/docs/javatutorial#compiling-your-protocol-buffers)

## Smart Contract
After modifying the `EventStorage.sol` smart contract, generate its Application Binary Interface (`.abi`) and Bytecode (`.bin`) by using the [Solidity Compiler](https://solidity.readthedocs.io/en/latest/installing-solidity.html) or [Remix].

After that, the `Setup` class of the `bdml.blockchain` module can be used to generate the Java wrapper class (using web3j) and deploying the contract to the connected blockchain.

[Remix]: https://remix.ethereum.org
[EventStorage smart contract]: bdml.blockchain/src/main/resources/EventStorage.sol
