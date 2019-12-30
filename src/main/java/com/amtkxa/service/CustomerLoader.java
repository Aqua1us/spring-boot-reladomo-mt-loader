package com.amtkxa.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amtkxa.common.exception.ReladomoMTLoaderException;
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
import com.gs.fw.common.mithra.util.QueueExecutor;
import com.gs.fw.common.mithra.util.SingleQueueExecutor;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class CustomerLoader {
    private static Logger log = LoggerFactory.getLogger(CustomerLoader.class.getName());
    private static int NUMBER_OF_THREADS = 2;
    private static int BATCH_SIZE = 5;
    private static int INSERT_THREADS = 3;

    public void load(MultipartFile file, Timestamp businessDate) {
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

            // Get current database records
            CustomerList customerList = CustomerFinder.findMany(
                    CustomerFinder.all()
                                  .and(CustomerFinder.businessDate().eq(businessDate))
                                  .and(CustomerFinder.processingDate().equalsInfinity())
            );

            // Database data load: Parallel
            DbLoadThread dbLoadThread = new DbLoadThread(customerList, null, matcherThread);
            dbLoadThread.start();

            // Input data load: Parallel
            PlainInputThread inputThread =
                    new PlainInputThread(new InputDataLoader(file, businessDate), matcherThread);
            inputThread.run();

            matcherThread.waitTillDone();
            log.info("The load of {} has been completed.", file.getOriginalFilename());
        } catch (Exception e) {
            throw new ReladomoMTLoaderException("Failed to load data. " + e.getMessage(), e.getCause());
        }
    }

    private class InputDataLoader implements InputLoader {
        private boolean firstTime = true;
        private Timestamp businessDate;
        private MultipartFile file;

        InputDataLoader(MultipartFile file, Timestamp businessDate) {
            this.file = file;
            this.businessDate = businessDate;
        }

        @Override
        public List<? extends MithraTransactionalObject> getNextParsedObjectList() {
            try {
                // Convert csv file to entity list
                List<CustomerCsvEntity> entityList =
                        new CsvToBeanBuilder(new InputStreamReader(file.getInputStream()))
                                .withType(CustomerCsvEntity.class)
                                .withSkipLines(1)
                                .build()
                                .parse();
                log.info("Loading uploaded csv file... filename: {}, line: {}, businessDate: {}",
                         file.getOriginalFilename(), entityList.size(), businessDate.toString());

                // Convert csv entity to table's entity
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
