package bdml.blockchain.callback;

import bdml.services.Blockchain;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockFinalizationCallbackManager implements Blockchain.BlockFinalizedListener {
    private static final ExecutorService executorPool = Executors.newWorkStealingPool();

    private Set<Blockchain.BlockFinalizedListener> registeredListeners = new HashSet<>();

    @Override
    public synchronized void newBlockFinalized(long blockNo) {
        for(Blockchain.BlockFinalizedListener listener: registeredListeners){
            executorPool.execute(() -> listener.newBlockFinalized(blockNo));
        }
    }

    public synchronized boolean addListener(Blockchain.BlockFinalizedListener listener) {
        return registeredListeners.add(listener);
    }

    public synchronized boolean removeListener(Blockchain.BlockFinalizedListener listener) {
        return registeredListeners.remove(listener);
    }
}
