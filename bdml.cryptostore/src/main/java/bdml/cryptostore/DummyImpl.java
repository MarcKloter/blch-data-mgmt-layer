package bdml.cryptostore;

import bdml.services.CryptographicStore;

import java.security.PublicKey;

public class DummyImpl {

    public static void main(String[] args) {
        CryptographicStore cs = new CryptoStoreAdapter();
        PublicKey pk = cs.generateKeyPair("mySecret");
        System.out.println(pk.getClass());
    }
}
