package bdml.blockchain.cache;

public class TxtIndex {
    public final long blockNo;
    public final long txtIndex;
    public final byte[] hash;
    public final boolean isPlain;

    public TxtIndex(long blockNo, long txtIndex, byte[] hash, boolean isPlain) {
        this.blockNo = blockNo;
        this.txtIndex = txtIndex;
        this.hash = hash;
        this.isPlain = isPlain;
    }
}
