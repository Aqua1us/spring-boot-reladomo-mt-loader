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
    private static int NUMBER_OB_THREADS = 1;
    private static int BATCH_SIZE = 1;
    private static int INSERT_THREADS = 1;

    private List<Customer> getInputData() throws ParseException {
        Timestamp businessDate = getTimestamp("2019-12-01 00:00:00");
        CustomerList customerList = new CustomerList();
        customerList.add(new Customer("Liam", "USA", businessDate));
        customerList.add(new Customer("Emma", "USA", businessDate));
        customerList.add(new Customer("Noah", "USA", businessDate));
        customerList.add(new Customer("Olivia", "USA", businessDate));
        customerList.add(new Customer("William", "USA", businessDate));
        customerList.add(new Customer("Ava", "USA", businessDate));
        customerList.add(new Customer("James", "USA", businessDate));
        return customerList;
    }

    private CustomerList getDbRecords() throws ParseException {
        Operation businessDate = CustomerFinder.businessDate().eq(getTimestamp("2019-12-01 00:00:00"));
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
    public void testDataLoad() throws ParseException {
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

        // Assert
        CustomerList customerList = getDbRecords();
        assertEquals(customerList.count(), 7);
    }
}
