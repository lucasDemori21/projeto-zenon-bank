package br.com.zenon;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionIngestor {

    private static final int FRAUD_LIMIT = 50_000;

    public List<Transaction> read(String filename) {
        Path path = Path.of(filename);

        try {
            List<String> lines = Files.readAllLines(path);
            return lines.stream()
                    .skip(1)
                    .limit(FRAUD_LIMIT)
                    .map(this::parseTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler arquivo " + filename, ex);
        }
    }

    private Optional<Transaction> parseTransaction(String lines) {
        try {
            String[] chunks = lines.split(",");

            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);

            if (chunks[2] == null || chunks[2].trim().isEmpty()) throw new IllegalArgumentException("Valor de amount não pode ser nulo e nem vazio");
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