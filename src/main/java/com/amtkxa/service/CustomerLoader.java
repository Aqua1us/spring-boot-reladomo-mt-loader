package com.amtkxa.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amtkxa.common.exception.ReladomoMTLoaderException;
import com.amtkxa.common.util.DateUtil;
import com.amtkxa.domain.entity.Customer;
import com.amtkxa.domain.entity.CustomerFinder;
import com.amtkxa.domain.entity.CustomerList;
import com.amtkxa.presentation.entity.CustomerCsvEntity;
import com.gs.fw.common.mithra.MithraTransactionalObject;
import com.gs.fw.common.mithra.extractor.Extractor;
import com.gs.fw.common.mithra.mtloader.DbLoadThread;
import com.gs.fw.common.mithra.mtloader.InputLoader;
import com.gs.fw.common.mithra.mtloader.MatcherThread;
import com.gs.fw.common.mithra.mtloader.PlainInputThread;
import com.gs.fw.common.mithra.util.SingleQueueExecutor;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class CustomerLoader {
    private static int NUMBER_OB_THREADS = 1;
    private static int BATCH_SIZE = 1;
    private static int INSERT_THREADS = 1;

    public void load(MultipartFile file) {
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
            CustomerList customerList = CustomerFinder.findMany(
                    CustomerFinder.all()
                                  .and(CustomerFinder.businessDate().eq(DateUtil.now()))
                                  .and(CustomerFinder.processingDate().equalsInfinity())
            );

            // Database data load: Parallel
            DbLoadThread dbLoadThread = new DbLoadThread(customerList, null, matcherThread);
            dbLoadThread.start();

            // Input data load: Parallel
            PlainInputThread inputThread = new PlainInputThread(new InputDataLoader(file), matcherThread);
            inputThread.run();

            matcherThread.waitTillDone();
        } catch (Exception e) {
            throw new ReladomoMTLoaderException("Failed to load data. " + e.getMessage(), e.getCause());
        }
    }

    private class InputDataLoader implements InputLoader {
        private boolean firstTime = true;
        private MultipartFile file;

        InputDataLoader(MultipartFile file) {
            this.file = file;
        }

        @Override
        public List<? extends MithraTransactionalObject> getNextParsedObjectList() {
            try {
                // convert csv file to entity list
                List<CustomerCsvEntity> entityList =
                        new CsvToBeanBuilder(new InputStreamReader(file.getInputStream()))
                                .withType(CustomerCsvEntity.class)
                                .withSkipLines(1)
                                .build()
                                .parse();

                // convert csv entity to table's entity
                Timestamp businessDate = DateUtil.now();
                return entityList
                        .stream()
                        .map(m -> new Customer(m.getCustomerId(), m.getName(), m.getCountry(), businessDate))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse csv to entity.");
            }
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
