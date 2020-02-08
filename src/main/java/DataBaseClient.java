import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;

public class DataBaseClient implements AutoCloseable {
    private static final String URL = "jdbc:mysql://localhost/SHOP_BASE?serverTimezone=europe/Moscow";
    private static final String TABLE_NAME = "catalogue";

    private static DataBaseClient instance = null;

    private Connection connection;

    private DataBaseClient(String username, String password) throws SQLException {
        connection = DriverManager.getConnection(URL, username, password);
        connection.setAutoCommit(false);

        if (!tableExists()) {
            createTable();
        }
    }

    public static DataBaseClient getInstance(String username, String password) throws SQLException {
        return instance == null ? instance = new DataBaseClient(username, password) : instance;
    }

    public void reset(int numOfProducts) throws SQLException {
        try (Statement truncateStatement = connection.createStatement();
             PreparedStatement insertStatement =
                     connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (prodid, title, cost) VALUES (?, ?, ?)")) {

            truncateStatement.execute("TRUNCATE TABLE " + TABLE_NAME);

            for (int i = 1; i < numOfProducts + 1; i++) {
                insertStatement.setString(1, UUID.randomUUID().toString());
                insertStatement.setString(2, "товар " + i);
                insertStatement.setInt(3, i * 100);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void addProduct(String title, int cost) throws SQLException {
        if (productExists(title)) {
            throw new IllegalArgumentException("Product already exists");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost can't be negative.");
        }

        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (prodid, title, cost) VALUES (?, ?, ?)")) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, title);
            statement.setInt(3, cost);
            statement.execute();

            connection.commit();
        }
    }

    public void deleteProduct(String title) throws SQLException {
        if (!productExists(title)) {
            throw new IllegalArgumentException("Product doesn't exist.");
        }

        try (PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE title = ?")) {
            statement.setString(1, title);
            statement.execute();

            connection.commit();
        }
    }

    public void updatePrice(String title, int cost) throws SQLException {
        if (!productExists(title)) {
            throw new IllegalArgumentException("Product doesn't exist.");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost can't be negative.");
        }

        try (PreparedStatement statement =
                     connection.prepareStatement("UPDATE " + TABLE_NAME + " SET cost = ? WHERE title = ?")) {
            statement.setInt(1, cost);
            statement.setString(2, title);
            statement.execute();

            connection.commit();
        }
    }

    public void applyToAll(Consumer<ResultSet> function) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME)) {
            while (resultSet.next()) {
                function.accept(resultSet);
            }
        }
    }

    public void applyToPriceRange(int costFrom, int costTo, Consumer<ResultSet> function) throws SQLException {
        if (costTo < costFrom) {
            throw new IllegalArgumentException("From must be lower then to");
        }
        if (costFrom < 0) {
            throw new IllegalArgumentException("Cost can't be negative.");
        }

        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE cost >= ? AND cost <= ?");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                function.accept(resultSet);
            }
        }
    }

    public boolean productExists(String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT title FROM " + TABLE_NAME + " WHERE title = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private boolean tableExists() throws SQLException {
        boolean tableExists = false;
        try (ResultSet resultSet =
                     connection.getMetaData().getTables(null, null, TABLE_NAME, null)) {
            while (resultSet.next()) {
                if (resultSet.getString("TABLE_NAME").equals(TABLE_NAME)) {
                    tableExists = true;
                    break;
                }
            }
        }
        return tableExists;
    }

    private void createTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE WAREHOUSE(" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "prodid VARCHAR(36) NOT NULL," +
                    "title VARCHAR(50) NOT NULL," +
                    "cost INT NOT NULL," +
                    "PRIMARY KEY(id)");
            connection.commit();
        }
    }
}
