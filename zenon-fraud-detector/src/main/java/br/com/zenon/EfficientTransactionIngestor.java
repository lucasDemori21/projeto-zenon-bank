package br.com.zenon;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {

    private static final int FRAUD_LIMIT = 10_000;
    private static final int LINE_BATCH_SIZE = 5_000;

    public void executeBatch(List<String> lineBatch, Consumer<List<Transaction>> batchConsumer) {
        List<Transaction> transactionBatch = lineBatch
                .stream()
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        batchConsumer.accept(transactionBatch);
    }

    public void readAsBatch(String filename, Consumer<List<Transaction>> batchConsumer) {
        Path path = Path.of(filename);

        try (Stream<String> lines = Files.lines(path).skip(1).limit(FRAUD_LIMIT)) {

            var iterator = lines.iterator();
            if (iterator.hasNext()) iterator.next();

            List<String> lineBatch = new ArrayList<>(LINE_BATCH_SIZE);
            while (iterator.hasNext()) {
                String line = iterator.next();
                lineBatch.add(line);

                if (lineBatch.size() >= LINE_BATCH_SIZE) {
                    executeBatch(lineBatch, batchConsumer);
                    lineBatch.clear();
                }
            }

            if (!lineBatch.isEmpty()) {
                IO.println("Executando Batch final ingestor JDBC");
                executeBatch(lineBatch, batchConsumer);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, ex);
        }
    }


    public void readAsStream(String filename, Consumer<Transaction> consumer) {
        Path path = Path.of(filename);

        try (Stream<String> lines = Files.lines(path)) {
            lines
                    .skip(1)
                    .limit(FRAUD_LIMIT)
                    .map(this::parseTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(consumer);

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, ex);
        }
    }

    private Optional<Transaction> parseTransaction(String lines) {
        try {
            String[] chunks = lines.split(",");

            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);

            if (chunks[2] == null || chunks[2].trim().isEmpty())
                throw new IllegalArgumentException("Valor de amount não pode ser nulo e nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            TransactionCustomer origin = new TransactionCustomer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
            TransactionCustomer recipient = new TransactionCustomer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));

            boolean isFraud = "1".equals(chunks[9]);
            boolean isFlaggedFraud = "1".equals(chunks[10]);

            return Optional.of(new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud));

        } catch (Exception exception) {
            System.err.println("Erro ao fazer parse: " + lines + " | " + exception);
        }

        return Optional.empty();
    }
}