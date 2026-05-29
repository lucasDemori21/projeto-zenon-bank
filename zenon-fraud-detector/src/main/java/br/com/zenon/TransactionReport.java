package br.com.zenon;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {

    private record ReportTransaction(BigDecimal amount, boolean isFraud) {
    }

    public record Statistics(long totalTransaction, long totalFrauds, BigDecimal totalAmount) {

        private final static Statistics ZERO = new Statistics(0,0,BigDecimal.ZERO);

        public Statistics addReportTransaction(ReportTransaction rt){
            return new Statistics(
                    totalTransaction + 1,
                    totalFrauds + (rt.isFraud ? 1 : 0),
                    totalAmount.add(rt.amount));
        }

        public Statistics add(Statistics other){
            return new Statistics(
                    totalTransaction + other.totalTransaction,
                    totalFrauds + other.totalFrauds,
                    totalAmount.add(other.totalAmount));
        }
    }

    public Statistics generateReport(String fileName) {
        Path path = Path.of(fileName);

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .map(this::parseReportTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(Statistics.ZERO, Statistics::addReportTransaction, Statistics::add);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler arquivo " + fileName, ex);
        }
    }

    private Optional<ReportTransaction> parseReportTransaction(String lines) {
        try {
            String[] chunks = lines.split(",");

            if (chunks[2] == null || chunks[2].trim().isEmpty()) throw new IllegalArgumentException("Valor de amount não pode ser nulo e nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);

            boolean isFraud = "1".equals(chunks[9]);

            return Optional.of(new ReportTransaction(amount, isFraud));

        } catch (Exception exception) {
            System.err.println("Erro ao fazer parse: " + lines + " | " + exception);
        }

        return Optional.empty();
    }
}
