package bdml.blockchain.cache;


import bdml.services.QueryResult;

import java.util.List;

public interface Cache {

    void addDocumentIndex(byte[] ident, TxtIndex txtIndex);
    void addAccessIndex(byte[] ident, long blockNo, byte[] hash);
    void addAmendIndex(byte[] ident, long blockNo, byte[] hash);

    void finalizeBlock(long no);
    long nextBlock();
    List<byte[]> getAllFinalizedAccessTokens(long blockNoStart, long blockNoEnd, byte[] ident);
    List<QueryResult<byte[]>> getAllPlain(long fromBlockInclusive, long toBlockExclusive);
    List<byte[]> getAllAmendments(long fromBlockInclusive, long toBlockExclusive);
    List<byte[]> getAllAmendmentFor(byte[] identifier);


    List<TxtIndex> getIndex(byte[] ident);

    void addPendingFrame(byte[] ident, byte[] frame, boolean encrypted);
    void addPendingToken(byte[] ident, byte[] token);
    void addPendingAmend(byte[] ident, byte[] token);
    void removePendingFrame(byte[] ident);
    void removePendingToken(byte[] ident, byte[] token);
    void removePendingAmend(byte[] ident, byte[] token);

    QueryResult<byte[]> getPendingDocument(byte[] ident);

}
