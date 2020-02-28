package db;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class ProductDAO {
    private static final String TABLE_NAME = "catalogue";

    private MysqlDataSource dataSource = new MysqlDataSource();

    public ProductDAO(String username, String password) {
        dataSource.setUrl("jdbc:mysql://localhost/SHOP_BASE?serverTimezone=Europe/Moscow");
        dataSource.setUser(username);
        dataSource.setPassword(password);

        try (Connection connection = dataSource.getConnection()) {

        } catch (SQLException e) {
            throw new RuntimeException("Error while connecting to database", e);
        }

        if (!tableExists()) {
            createTable();
        }
    }

    public void add(Product product) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (prodid, title, cost) VALUES (?, ?, ?)")) {
            statement.setString(1, product.getProdId());
            statement.setString(2, product.getTitle());
            statement.setInt(3, product.getCost());
            statement.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Product already exists");
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding new product", e);
        }
    }

    public void add(Collection<Product> list) {
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
            } catch (BatchUpdateException e) {
                if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    throw new IllegalArgumentException("Product already exists");
                }

                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding new products", e);
        }

    }

    public Product getById(int id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return toProduct(resultSet);
                }

                throw new IllegalArgumentException("No such id.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving product by id", e);
        }
    }

    public Product getByName(String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE title = ?")) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return toProduct(resultSet);
                }

                throw new IllegalArgumentException("No such name.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving product by name", e);
        }
    }

    public Product getByProdId(String prodId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE prodId = ?")) {
            statement.setString(1, prodId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return toProduct(resultSet);
                }

                throw new IllegalArgumentException("No such product id.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving product by product id", e);
        }
    }

    public ArrayList<Product> list() {
        ArrayList<Product> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM catalogue")) {
                while (resultSet.next()) {
                    list.add(toProduct(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving list of products", e);
        }

        return list;
    }

    public ArrayList<Product> listFromPriceRange(int costFrom, int costTo) {
        if (costTo < costFrom) {
            throw new IllegalArgumentException("From must be lower then to");
        }
        if (costFrom < 0) {
            throw new IllegalArgumentException("Cost can't be negative.");
        }

        ArrayList<Product> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE cost >= ? AND cost <= ?")) {
            statement.setInt(1, costFrom);
            statement.setInt(2, costTo);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(toProduct(resultSet));
                }
            }
            if (list.isEmpty()) {
                throw new IllegalArgumentException("No products in price range.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving list of products", e);
        }

        return list;
    }

    public void updatePrice(String name, int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("Price can't be negative");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE_NAME + " SET cost = ? WHERE title = ?")) {
            statement.setInt(1, cost);
            statement.setString(2, name);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No such product");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating price", e);
        }
    }

    public void delete(String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE title = ?")) {
            statement.setString(1, name);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No such product");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while removing product", e);
        }
    }

    public void clear() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE " + TABLE_NAME);
        } catch (SQLException e) {
            throw new RuntimeException("Error while clearing table", e);
        }
    }

    private Product toProduct(ResultSet resultSet) {
        try {
            return new Product(
                    resultSet.getInt("id"),
                    resultSet.getString("prodid"),
                    resultSet.getString("title"),
                    resultSet.getInt("cost")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error while extracting data", e);
        }
    }

    private boolean tableExists() {
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet =
                     connection.getMetaData().getTables(null, null, TABLE_NAME, null)) {
            while (resultSet.next()) {
                if (resultSet.getString("TABLE_NAME").equals(TABLE_NAME)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while checking for table existence", e);
        }
        return false;
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + TABLE_NAME + "(" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "prodid VARCHAR(36) NOT NULL," +
                    "title VARCHAR(50) NOT NULL," +
                    "cost INT NOT NULL," +
                    "PRIMARY KEY(id), UNIQUE(prodid), UNIQUE(title))");
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating table", e);
        }
    }
}
