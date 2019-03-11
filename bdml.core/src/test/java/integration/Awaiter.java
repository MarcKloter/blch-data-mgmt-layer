package integration;

import bdml.core.CoreService;
import bdml.core.PersonalCore;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.exceptions.DataUnavailableException;
import bdml.services.Blockchain;
import bdml.services.QueryResult;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Awaiter {
    public static void await(DataIdentifier identifier, PersonalCore core) throws Exception {
        assertNotNull(identifier);
        assertNotNull(core);
        final Semaphore waiter = new Semaphore(0);
        Blockchain.BlockFinalizedListener listener = no -> {
            QueryResult<Data> data = null;
            try {
                data = core.getData(identifier);
            } catch (DataUnavailableException ignored) { }
            if(data != null && data.inclusionTime != null){
                waiter.release();
            }
        };
        assertTrue(CoreService.getInstance().addUpdateListener(listener));
        //check if it is already their
        try {
            QueryResult<Data> data = core.getData(identifier);
            if(data != null && data.inclusionTime != null) {
                assertTrue(CoreService.getInstance().removeUpdateListener(listener));
                return;
            }
        } catch (DataUnavailableException ignored) { }

        assertTrue(waiter.tryAcquire(50, TimeUnit.SECONDS));
        assertTrue(CoreService.getInstance().removeUpdateListener(listener));
    }

}
