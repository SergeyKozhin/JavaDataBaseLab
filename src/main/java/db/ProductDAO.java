package db;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

public class ProductDAO {
    private static final String TABLE_NAME = "catalogue";

    private static MysqlDataSource dataSource = new MysqlDataSource();

    static {
        dataSource.setUrl("jdbc:mysql://localhost/SHOP_BASE?serverTimezone=Europe/Moscow");
        dataSource.setUser("test");
        dataSource.setPassword("password");
    }

    public ProductDAO() throws SQLException {
        if (!tableExists()) {
            createTable();
        }
    }

    public void add(Product product) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (prodid, title, cost) VALUES (?, ?, ?)")) {
            statement.setString(1, product.getProdId());
            statement.setString(2, product.getTitle());
            statement.setInt(3, product.getCost());
            statement.execute();
        }
    }

    public void add(Collection<Product> list) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (prodid, title, cost) VALUES (?, ?, ?)")) {
                for (Product product : list) {
                    statement.setString(1, product.getProdId());
                    statement.setString(2, product.getTitle());
                    statement.setInt(3, product.getCost());
                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }

    }

    public Product getById(int id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return toProduct(resultSet);
                }

                throw new IllegalArgumentException("No such id.");
            }
        }
    }

    public Product getByName(String name) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE title = ?")) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return toProduct(resultSet);
                }

                throw new IllegalArgumentException("No such name.");
            }
        }
    }

    public Product getByProdId(String prodId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE prodId = ?")) {
            statement.setString(1, prodId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return toProduct(resultSet);
                }

                throw new IllegalArgumentException("No such product id.");
            }
        }
    }

    public void updatePrice(String name, int cost) throws SQLException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE_NAME + " SET cost = ? WHERE title = ?")) {
            statement.setInt(1, cost);
            statement.setString(2, name);
            statement.execute();
        }
    }

    public void remove(String name) throws SQLException {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE title = ?")) {
            statement.setString(1, name);
            statement.execute();
        }
    }

    private Product toProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("prodid"),
                resultSet.getString("title"),
                resultSet.getInt("cost")
        );
    }

    private boolean tableExists() throws SQLException {
        try (Connection connection = dataSource.getConnection();
            ResultSet resultSet =
                     connection.getMetaData().getTables(null, null, TABLE_NAME, null)) {
            while (resultSet.next()) {
                if (resultSet.getString("TABLE_NAME").equals(TABLE_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createTable() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + TABLE_NAME + "(" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "prodid VARCHAR(36) NOT NULL," +
                    "title VARCHAR(50) NOT NULL," +
                    "cost INT NOT NULL," +
                    "PRIMARY KEY(id), UNIQUE(prodid), UNIQUE(title) )");
        }
    }
}
