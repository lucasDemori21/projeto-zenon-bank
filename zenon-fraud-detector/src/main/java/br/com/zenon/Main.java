package br.com.zenon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {

    void main() {
        var t1 = new Transaction(1, TransactionType.PAYMENT, new BigDecimal("9839.64"),
                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")), false, false);

        var t2 = new Transaction(743, TransactionType.CASH_OUT, new BigDecimal("850002.52"),
                new TransactionCustomer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new TransactionCustomer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")), true, false);

        IO.println(t1);
        IO.println(t2);

        IO.println("---------------------------------------------");

        TransactionIngestor transactionIngestor = new TransactionIngestor();
        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());

        IO.println("---------------------------------------------");

        FraudAnalyzer fraudAnalyzer = new FraudAnalyzer(transactions);

        long countFrauds = fraudAnalyzer.countFrauds();
        IO.println("Total de fraudes: " + countFrauds);

        List<Transaction> highestValueFrauds = fraudAnalyzer.findHighestValueFrauds(3);
        IO.println("Top 3 fraudes de maior valor:");
        highestValueFrauds.stream().map(Transaction::amount).forEach(IO::println);

        List<String> suspectCustomer = fraudAnalyzer.findSuspectCustomers(5);
        IO.println("Top 5 clientes suspeitos:");
        suspectCustomer.forEach(IO::println);

        BigDecimal totalFraudLoss = fraudAnalyzer.calculateTotalFraudLoss(5);
        IO.println("Prejuizo total: R$ " + totalFraudLoss);

        Map<TransactionType, Long> countFraudsByType = fraudAnalyzer.countFraudsByType();
        IO.println("Fraudes por tipo:");
        IO.println(countFraudsByType);

    }
}
