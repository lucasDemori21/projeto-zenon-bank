package br.com.zenon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {

    private final static int JDBC_BATCH_SIZE = 1_000;

    public void save(Transaction transaction) {
        String sql = """
                INSERT INTO transactions (step, type, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient, old_balance_origin_recipient, new_balance_origin_recipient, is_fraud, is_flagged_fraud)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transaction.step());
            ps.setString(2, transaction.type().name());
            ps.setBigDecimal(3, transaction.amount());

            ps.setString(4, transaction.origin().name());
            ps.setBigDecimal(5, transaction.origin().oldBalance());
            ps.setBigDecimal(6, transaction.origin().newBalance());

            ps.setString(7, transaction.recipient().name());
            ps.setBigDecimal(8, transaction.recipient().oldBalance());
            ps.setBigDecimal(9, transaction.recipient().newBalance());

            ps.setBoolean(10, transaction.isFraud());
            ps.setBoolean(11, transaction.isFlaggedFraud());

            IO.println("Salvando transação: " + transaction.step() + " / " + transaction.amount());

            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar nova transação: " + e);
        }
    }

    @Override
    public Optional<Transaction> findByOriginName(String originName) {
        String sql = """
                SELECT id, step, `type`, amount, name_origin, old_balance_origin,
                new_balance_origin, name_recipient, old_balance_origin_recipient, new_balance_origin_recipient, 
                is_fraud, is_flagged_fraud 
                FROM zenon_frauds.transactions
                WHERE name_origin = ?
                """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, originName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    IO.println(rs.getString("name_origin"));
                    return Optional.of(transaction);
                } else {
                    IO.println("Transação não encontrada para origin: " + originName);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) {
        try {
            int step = rs.getInt("step");
            TransactionType transactionType = TransactionType.valueOf(rs.getString("type"));
            BigDecimal amount = rs.getBigDecimal("amount");

            String originName = rs.getString("name_origin");
            BigDecimal originOldBalance = rs.getBigDecimal("old_balance_origin");
            BigDecimal originNewBalance = rs.getBigDecimal("new_balance_origin");
            TransactionCustomer origin = new TransactionCustomer(originName, originOldBalance, originNewBalance);

            String recipientnName = rs.getString("name_recipient");
            BigDecimal recipientOldBalance = rs.getBigDecimal("old_balance_origin_recipient");
            BigDecimal recipientNewBalance = rs.getBigDecimal("new_balance_origin_recipient");
            TransactionCustomer recipient = new TransactionCustomer(recipientnName, recipientOldBalance, recipientNewBalance);

            boolean isFraud = rs.getBoolean("is_fraud");
            boolean isFlaggedFraud = rs.getBoolean("is_flagged_fraud");

            return new Transaction(step, transactionType, amount, origin, recipient, isFraud, isFlaggedFraud);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveAll(List<Transaction> transactions) {
        String sql = """
                INSERT INTO transactions (step, type, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient, old_balance_origin_recipient, new_balance_origin_recipient, is_fraud, is_flagged_fraud)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            int count = 0;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                for (Transaction transaction : transactions) {
                    ps.setInt(1, transaction.step());
                    ps.setString(2, transaction.type().name());
                    ps.setBigDecimal(3, transaction.amount());

                    ps.setString(4, transaction.origin().name());
                    ps.setBigDecimal(5, transaction.origin().oldBalance());
                    ps.setBigDecimal(6, transaction.origin().newBalance());

                    ps.setString(7, transaction.recipient().name());
                    ps.setBigDecimal(8, transaction.recipient().oldBalance());
                    ps.setBigDecimal(9, transaction.recipient().newBalance());

                    ps.setBoolean(10, transaction.isFraud());
                    ps.setBoolean(11, transaction.isFlaggedFraud());

                    ps.execute();

                    ps.addBatch();
                    count++;

                    if (count % JDBC_BATCH_SIZE == 0) {
                        IO.println("Executando batch...");
                        ps.executeBatch();
                        conn.commit();
                    }
                }

                IO.println("Executando batch final...");
                ps.executeBatch();
                conn.commit();

                conn.setAutoCommit(true);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erro ao executar rollback: " + ex);
                }
                throw new RuntimeException("Erro ao salvar nova transação: " + e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro na conexão com o banco de dados: " + e);
        }
    }
}
