package br.com.zenon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class DBMain {

    void main() {
        ConnectionFactory.getConnection();
        IO.println("Conexão com o DB criada! :)");

        TransactionSQLRepository repository = new TransactionSQLRepository();
        repository.findByOriginName("C10000001")
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para C1000001"));
        repository.findByOriginName("C12345")
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para C12345"));

        TransactionIngestor transactionIngestor = new TransactionIngestor();

        long startTimeSQL = System.nanoTime();
        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());

        IO.println("Iniciando as transações no BD...");
        transactions.forEach(repository::save);

        long endTimeSQL = System.nanoTime();
        IO.println("Tempo de busca - List (ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);
    }
}
