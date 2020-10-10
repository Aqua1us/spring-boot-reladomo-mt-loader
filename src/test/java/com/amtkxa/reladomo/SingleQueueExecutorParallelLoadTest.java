package com.amtkxa.reladomo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.amtkxa.common.exception.ReladomoMTLoaderException;
import com.amtkxa.common.util.DateUtil;
import com.amtkxa.domain.entity.Customer;
import com.amtkxa.domain.entity.CustomerFinder;
import com.amtkxa.domain.entity.CustomerList;
import com.gs.fw.common.mithra.MithraTransactionalObject;
import com.gs.fw.common.mithra.extractor.Extractor;
import com.gs.fw.common.mithra.mtloader.DbLoadThread;
import com.gs.fw.common.mithra.mtloader.InputLoader;
import com.gs.fw.common.mithra.mtloader.MatcherThread;
import com.gs.fw.common.mithra.mtloader.PlainInputThread;
import com.gs.fw.common.mithra.util.QueueExecutor;
import com.gs.fw.common.mithra.util.SingleQueueExecutor;

public class SingleQueueExecutorParallelLoadTest extends AbstractReladomoTest {
    private static int NUMBER_OF_THREADS = 2;
    private static int BATCH_SIZE = 5;
    private static int INSERT_THREADS = 3;

    @Override
    public String[] getTestDataFilenames() {
        return new String[] { "testdata/customer_data.txt" };
    }

    private List<Customer> getInputData() {
        Timestamp businessDate = DateUtil.parse("2019-12-05 00:00:00");
        CustomerList customerList = new CustomerList();
        customerList.add(new Customer(6, "Ava", "JPN", businessDate));
        customerList.add(new Customer(8, "Arthur", "USA", businessDate));
        return customerList;
    }

    private CustomerList getDbRecords() {
        return CustomerFinder.findMany(
                CustomerFinder.all()
                              .and(CustomerFinder.businessDate().equalsEdgePoint())
                              .and(CustomerFinder.processingDate().equalsInfinity())
        );
    }

    @Test
    public void testLoadDataParallel() {
        try {
            QueueExecutor queueExecutor = new SingleQueueExecutor(
                    NUMBER_OF_THREADS,
                    CustomerFinder.customerId().ascendingOrderBy(),
                    BATCH_SIZE,
                    CustomerFinder.getFinderInstance(),
                    INSERT_THREADS
            );

            MatcherThread<Customer> matcherThread = new MatcherThread<>(
                    queueExecutor,
                    new Extractor[] { CustomerFinder.customerId() }
            );
            matcherThread.start();

            // Database data load: Parallel
            DbLoadThread dbLoadThread = new DbLoadThread(getDbRecords(), null, matcherThread);
            dbLoadThread.start();

            // Input data load: Parallel
            PlainInputThread inputThread = new PlainInputThread(new InputDataLoader(), matcherThread);
            inputThread.run();
            matcherThread.waitTillDone();

            // Assert
            checkResult(queueExecutor);
        } catch (Exception e) {
            throw new ReladomoMTLoaderException("Failed to load data. " + e.getMessage(), e.getCause());
        }
    }

    private void checkResult(QueueExecutor queueExecutor) {
        // Whatever is in Output Set but not in Input Set will be closed out (terminated).
        CustomerList customerList = getDbRecords();
        assertEquals(2, customerList.count());

        // Whatever is in the intersection, will be updated (but only if something changed)
        Customer customer = CustomerFinder.findOne(
                CustomerFinder.customerId().eq(6)
                              .and(CustomerFinder.businessDate().equalsEdgePoint())
                              .and(CustomerFinder.processingDate().equalsInfinity())
        );
        assertAll("Check updated customer data",
                  () -> assertEquals("Ava", customer.getName()),
                  () -> assertEquals("JPN", customer.getCountry()) // Updated from USD to JPN
        );

        // Whatever in in Input Set but not in Output Set will be inserted
        Customer customer8 = CustomerFinder.findOne(
                CustomerFinder.customerId().eq(8)
                              .and(CustomerFinder.businessDate().equalsEdgePoint())
                              .and(CustomerFinder.processingDate().equalsInfinity())
        );
        assertAll("Check inserted customer data",
                  () -> assertEquals("Arthur", customer8.getName()),
                  () -> assertEquals("USA", customer8.getCountry())
        );

        assertAll("Check the count of inserts, updates, terminates",
                  () -> assertEquals(1, queueExecutor.getTotalInserts()),
                  () -> assertEquals(1, queueExecutor.getTotalUpdates()),
                  () -> assertEquals(6, queueExecutor.getTotalTerminates())
        );
    }

    private class InputDataLoader implements InputLoader {
        private boolean firstTime = true;

        @Override
        public List<? extends MithraTransactionalObject> getNextParsedObjectList() {
            return getInputData();
        }

        @Override
        public boolean isFileParsingComplete() {
            if (firstTime) {
                firstTime = false;
                return false;
            } else {
                return true;
            }
        }
    }
}
