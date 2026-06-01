package br.com.zenon;

import java.util.List;

public class IngestionMain {

    void main() {
        TransactionSQLRepository repository = new TransactionSQLRepository();

        EfficientTransactionIngestor efficientTransactionIngestor = new EfficientTransactionIngestor();

        repository.findByOriginName("C10000001")
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para C1000001"));
        repository.findByOriginName("C12345")
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para C12345"));

        long startTimeSQL = System.nanoTime();
        efficientTransactionIngestor.readAsBatch("data/PS_20174392719_1491204439457_log.csv", repository::saveAll);

//        repository.saveAll(transactions);

        long endTimeSQL = System.nanoTime();
        IO.println("Tempo de ingestão no Database (ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);
    }
}
