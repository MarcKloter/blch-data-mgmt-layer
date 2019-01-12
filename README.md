# blch-data-mgmt-layer

## Requirements
The proof of concept is built to interact with [Parity Ethereum](https://www.parity.io/ethereum/) and requires an activated `personal` module.

## Running locally
### Parity
Windows:
```
parity.exe --chain dev --jsonrpc-apis=all --jsonrpc-cors=all
```

MacOS:
```
parity --chain dev --jsonrpc-apis=all --jsonrpc-cors=all
```

### JSON-RPC Endpoint
To run the JSON-RPC Endpoint a keystore file specifying key material for SSL is required.
For development a keypair can be generated using the Java Keytool: 
```
keytool -genkey -keyalg RSA -keysize 2048 -keystore keystore.jks
```

A relative path to this .jks file must be supplied to the jar using the `--keystore` argument along with the `--password` to retrieve the keys:

```
java -jar blch-data-mgmt-layer-1.0.0.jar --keystore keystore.jks --password myPassword
```

## Protocol Buffers
After modifying `.proto` files, their Java source files need to be regenerated using the Protocol Buffers Compiler for the changes to take effect.

Instructions: [Compiling Your Protocol Buffers](https://developers.google.com/protocol-buffers/docs/javatutorial#compiling-your-protocol-buffers)

## Smart Contract
After modifying the `EventStorage.sol` smart contract, generate its Application Binary Interface (`.abi`) and Bytecode (`.bin`) by using the [Solidity Compiler](https://solidity.readthedocs.io/en/latest/installing-solidity.html) or [Remix](https://remix.ethereum.org).

After that, the `Setup` class of the `bdml.blockchain` module can be used to generate the Java wrapper class (using web3j) and deploying the contract to the connected blockchain.

