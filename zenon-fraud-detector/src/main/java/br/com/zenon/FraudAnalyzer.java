package br.com.zenon;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FraudAnalyzer {

    private final List<Transaction> transactionList;

    public FraudAnalyzer(List<Transaction> transactionList) {
        Objects.requireNonNull(transactionList);
        this.transactionList = transactionList;
    }

    public long countFrauds() {
        return transactionList
                .stream()
                .filter(Transaction::isFraud)
                .count();
    }

    public List<Transaction> findHighestValueFrauds(int limit) {
        return transactionList
                .stream()
                .filter(Transaction::isFraud)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .limit(limit)
                .toList();
    }

    public List<String> findSuspectCustomers(int limit) {
        return transactionList
                .stream()
                .filter(Transaction::isFraud)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .map(transactionList -> transactionList.origin().name())
                .distinct()
                .limit(limit)
                .toList();
    }

    public BigDecimal calculateTotalFraudLoss(int limit) {
        return transactionList
                .stream()
                .filter(Transaction::isFraud)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public Map<TransactionType, Long> countFraudsByType() {
        return transactionList
                .stream()
                .filter(Transaction::isFraud)
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }
}
