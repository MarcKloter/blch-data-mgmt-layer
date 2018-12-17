package bdml.blockchain.parity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Transaction {
    private String hash;
    private String nonce;
    private String blockHash;
    private String blockNumber;
    private String transactionIndex;
    private String from;
    private String to;
    private String value;
    private String gasPrice;
    private String gas;
    private String input;
    private String v;
    private String standardV;
    private String s;
    private String r;
    private String raw;
    private String publicKey;
    private String chainId;
    private String creates;
    private String condition;

    @JsonCreator
    public Transaction(@JsonProperty("hash") String hash,
                       @JsonProperty("nonce") String nonce,
                       @JsonProperty("blockHash") String blockHash,
                       @JsonProperty("blockNumber") String blockNumber,
                       @JsonProperty("transactionIndex") String transactionIndex,
                       @JsonProperty("from") String from,
                       @JsonProperty("to") String to,
                       @JsonProperty("value") String value,
                       @JsonProperty("gasPrice") String gasPrice,
                       @JsonProperty("gas") String gas,
                       @JsonProperty("input") String input,
                       @JsonProperty("v") String v,
                       @JsonProperty("standardV") String standardV,
                       @JsonProperty("s") String s,
                       @JsonProperty("r") String r,
                       @JsonProperty("raw") String raw,
                       @JsonProperty("publicKey") String publicKey,
                       @JsonProperty("chainId") String chainId,
                       @JsonProperty("creates") String creates,
                       @JsonProperty("condition") String condition) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.input = input;
        this.v = v;
        this.standardV = standardV;
        this.r = r;
        this.raw = raw;
        this.publicKey = publicKey;
        this.chainId = chainId;
        this.creates = creates;
        this.condition = condition;
    }

    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    @JsonProperty("nonce")
    public String getNonce() {
        return nonce;
    }

    @JsonProperty("blockHash")
    public String getBlockHash() {
        return blockHash;
    }

    @JsonProperty("blockNumber")
    public String getBlockNumber() {
        return blockNumber;
    }

    @JsonProperty("transactionIndex")
    public String getTransactionIndex() {
        return transactionIndex;
    }

    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("gasPrice")
    public String getGasPrice() {
        return gasPrice;
    }

    @JsonProperty("gas")
    public String getGas() {
        return gas;
    }

    @JsonProperty("data")
    public String getInput() {
        return input;
    }

    @JsonProperty("v")
    public String getV() {
        return v;
    }

    @JsonProperty("standardV")
    public String getStandardV() {
        return standardV;
    }

    @JsonProperty("s")
    public String getS() {
        return s;
    }

    @JsonProperty("r")
    public String getR() {
        return r;
    }

    @JsonProperty("raw")
    public String getRaw() {
        return raw;
    }

    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    @JsonProperty("chainId")
    public String getChainId() {
        return chainId;
    }

    @JsonProperty("creates")
    public String getCreates() {
        return creates;
    }

    @JsonProperty("condition")
    public String getCondition() {
        return condition;
    }

    @JsonProperty("input")
    public void setInput(String input) {
        this.input = input;
    }

    // builder pattern
    @JsonIgnoreType
    public static class Builder {
        // default values
        private String hash = null;
        private String nonce = null;
        private String blockHash = null;
        private String blockNumber = null;
        private String transactionIndex = null;
        private String from = null;
        private String to = null;
        private String value = null;
        private String gasPrice = null;
        private String gas = null;
        private String input = null;
        private String v = null;
        private String standardV = null;
        private String s = null;
        private String r = null;
        private String raw = null;
        private String publicKey = null;
        private String chainId = null;
        private String creates = null;
        private String condition = null;

        public Builder withFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder withData(String data) {
            this.input = data;
            return this;
        }

        public Transaction build() {
            return new Transaction(hash, nonce, blockHash, blockNumber, transactionIndex, from, to, value, gasPrice, gas, input, v, standardV, s, r, raw, publicKey, chainId, creates, condition);
        }
    }
}
