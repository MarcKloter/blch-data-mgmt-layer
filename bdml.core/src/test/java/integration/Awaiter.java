package integration;

import bdml.core.CoreService;
import bdml.core.PersonalCore;
import bdml.core.domain.Data;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.exceptions.DataUnavailableException;
import bdml.services.Blockchain;
import bdml.services.Pair;
import bdml.services.QueryResult;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Awaiter {

    @FunctionalInterface
    interface Query<I> {
        boolean run(PersonalCore core, I input) throws DataUnavailableException;
    }

    public static <I> void await(I input, PersonalCore core, Query<I> query) throws Exception {
        assertNotNull(input);
        assertNotNull(core);
        final Semaphore waiter = new Semaphore(0);
        Blockchain.BlockFinalizedListener listener = no -> {
            try {
                if(query.run(core,input)){
                    waiter.release();
                }
            } catch (DataUnavailableException ignored) { }
        };
        assertTrue(CoreService.getInstance().addUpdateListener(listener));
        //check if it is already their
        try {
            if(query.run(core,input)){
                assertTrue(CoreService.getInstance().removeUpdateListener(listener));
                return;
            }
        } catch (DataUnavailableException ignored) { }

        assertTrue(waiter.tryAcquire(50, TimeUnit.SECONDS));
        assertTrue(CoreService.getInstance().removeUpdateListener(listener));
    }


    public static void awaitData(DataIdentifier identifier, PersonalCore core) throws Exception {
        await(identifier,core, (ctx, input) -> {
            QueryResult<?> res = ctx.getData(input, true);
            return res != null && res.inclusionTime != null;
        });
    }

    public static void awaitPermanentData(DataIdentifier identifier, PersonalCore core) throws Exception {
        await(identifier,core, (ctx, input) -> {
            QueryResult<Data> res = ctx.getData(input, false);
            return res!= null && res.data != null;
        });
    }

    public static void awaitPermanentAmendment(DataIdentifier source, DataIdentifier amend, PersonalCore core) throws Exception {
        await(new Pair<>(source,amend),core, (ctx, input) -> ctx.listAmendmentsToData(input.first, false).contains(input.second));
    }

    public static void awaitPermanentAttachement(DataIdentifier source, DataIdentifier target, PersonalCore core) throws Exception {
        await(new Pair<>(source,target),core, (ctx, input) -> ctx.listAttachmentsToData(input.first, false).contains(input.second));
    }
}
