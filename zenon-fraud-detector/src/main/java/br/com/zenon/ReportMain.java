package br.com.zenon;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportMain {

    void main () {
        Locale locale = Locale.of("en");

        NumberFormat integerFormatter = NumberFormat.getIntegerInstance(locale);
        NumberFormat currencyFormatter = DecimalFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(Currency.getInstance("USD"));

        ResourceBundle resourceBundle = ResourceBundle.getBundle("report", locale);

        TransactionReport transactionReport = new TransactionReport();
        TransactionReport.Statistics statistics = transactionReport.generateReport("data/PS_20174392719_1491204439457_log.csv");

        String fmtTotalTransactions = integerFormatter.format(statistics.totalTransaction());
        String fmtmattedTotalFrauds = integerFormatter.format(statistics.totalFrauds());
        String fmtmattedTotalAmount = currencyFormatter.format(statistics.totalAmount());

        String msgTotalTransactions = resourceBundle.getString("label.total.transactions");
        String msgTotalFrauds = resourceBundle.getString("label.total.frauds");
        String msgTotalAmount = resourceBundle.getString("label.total.amount");

        IO.println("""
            %s: %s
            %s: %s
            %s: %s
            """.formatted(
                msgTotalTransactions, fmtTotalTransactions,
                msgTotalFrauds, fmtmattedTotalFrauds,
                fmtmattedTotalAmount, fmtmattedTotalAmount
            )
        );
    }
}
