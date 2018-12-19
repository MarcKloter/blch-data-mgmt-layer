package bdml.cryptostore.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeystoreFile {
	public String id;
	public Integer version;
	public String address;
	public Crypto crypto;
}
