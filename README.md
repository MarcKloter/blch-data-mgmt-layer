# blch-data-mgmt-layer

## 
To run the JSON-RPC Endpoint, a keystore file for SSL is required.
For development, a keypair can be generated using the keytool. 
```
keytool -genkey -keyalg RSA -keysize 2048 -keystore keystore.jks
```

A relative path to this .jks file must be supplied to the jar using the `--keystore` argument along with the `--password` to retrieve the keys.

```
java -jar blch-data-mgmt-layer-1.0.0.jar --keystore keystore.jks --password myPassword
```