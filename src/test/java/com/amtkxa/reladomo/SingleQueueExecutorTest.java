package com.amtkxa.reladomo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.amtkxa.common.exception.ReladomoMTLoaderException;
import com.amtkxa.domain.entity.Customer;
import com.amtkxa.domain.entity.CustomerFinder;
import com.amtkxa.domain.entity.CustomerList;
import com.gs.fw.common.mithra.extractor.Extractor;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.mtloader.MatcherThread;
import com.gs.fw.common.mithra.util.SingleQueueExecutor;

public class SingleQueueExecutorTest extends AbstractReladomoTest {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static int NUMBER_OB_THREADS = 2;
    private static int BATCH_SIZE = 5;
    private static int INSERT_THREADS = 3;

    @Override
    public String[] getTestDataFilenames() {
        return new String[] { "testdata/customer_data.txt" };
    }

    private List<Customer> getInputData() throws ParseException {
        Timestamp businessDate = getTimestamp("2019-12-05 00:00:00");
        CustomerList customerList = new CustomerList();
        customerList.add(new Customer(7, "Ava", "JPN", businessDate));
        customerList.add(new Customer(8, "Arthur", "USA", businessDate));
        return customerList;
    }

    private CustomerList getDbRecords() {
        Operation businessDate = CustomerFinder.businessDate().equalsEdgePoint();
        Operation processingDate = CustomerFinder.processingDate().equalsInfinity();
        return CustomerFinder.findMany(
                CustomerFinder.all()
                              .and(businessDate)
                              .and(processingDate));
    }

    private Timestamp getTimestamp(String date) throws ParseException {
        return new Timestamp(format.parse(date).getTime());
    }

    @Test
    public void testDataLoad() {
        try {
            SingleQueueExecutor singleQueueExecutor = new SingleQueueExecutor(
                    NUMBER_OB_THREADS,
                    CustomerFinder.customerId().ascendingOrderBy(),
                    BATCH_SIZE,
                    CustomerFinder.getFinderInstance(),
                    INSERT_THREADS
            );

            MatcherThread<Customer> matcherThread = new MatcherThread<>(
                    singleQueueExecutor,
                    new Extractor[] { CustomerFinder.customerId() }
            );
            matcherThread.start();

            // database records
            matcherThread.addDbRecords(getDbRecords());
            matcherThread.setDbDone();

            // input data
            matcherThread.addFileRecords(getInputData());
            matcherThread.setFileDone();
            matcherThread.waitTillDone();
        } catch (Exception e) {
            throw new ReladomoMTLoaderException("Failed to load data. " + e.getMessage(), e.getCause());
        }

        // Whatever is in Output Set but not in Input Set will be closed out (terminated).
        CustomerList customerList = getDbRecords();
        assertEquals(2, customerList.count());

        // Whatever is in the intersection, will be updated (but only if something changed)
        Customer customer7 = CustomerFinder.findOne(
                CustomerFinder.customerId().eq(7)
                              .and(CustomerFinder.businessDate().equalsEdgePoint())
                              .and(CustomerFinder.processingDate().equalsInfinity())
        );
        assertEquals("Ava", customer7.getName());
        assertEquals("JPN", customer7.getCountry()); // Updated from USD to JPN

        // Whatever in in Input Set but not in Output Set will be inserted
        Customer customer8 = CustomerFinder.findOne(
                CustomerFinder.customerId().eq(8)
                              .and(CustomerFinder.businessDate().equalsEdgePoint())
                              .and(CustomerFinder.processingDate().equalsInfinity())
        );
        assertEquals("Arthur", customer8.getName()); // Inserted new customer
        assertEquals("USA", customer8.getCountry());
    }
}
