package bdml.keyserver;

import bdml.services.KeyServer;

public class DummyImpl {

    public static void main(String[] args) {
        KeyServer ks = new KeyServerAdapter();
        System.out.println(ks.queryKey("76358d17d9656483e92600aae3bb6457be6ef028"));

        // additional code (determine algorithm from public key encoded format)
        //AsymmetricKeyParameter tst = PublicKeyFactory.createKey(Hex.decode(encodedKey));
        //ECPublicKeyParameters tst2 = (ECPublicKeyParameters)tst;
        //ECPoint tst3 = tst2.getParameters().getCurve().decodePoint(Hex.decode(encodedKey));
        // https://stackoverflow.com/questions/42911637/get-publickey-from-key-bytes-not-knowing-the-key-algorithm
        // https://stackoverflow.com/questions/20070349/how-to-determine-the-algorithm-used-to-generate-a-private-key-from-a-pem-file-u
    }
}
