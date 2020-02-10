package db;

import java.util.UUID;

public class Product {
    private int id;
    private String prodId;
    private String title;
    private int cost;

    public Product() {

    }

    public Product(int id, String prodId, String title, int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("Price can't be negative");
        }

        this.id = id;
        this.prodId = prodId;
        this.title = title;
        this.cost = cost;
    }

    public Product(String prodId, String title, int cost) {
        this(0, prodId, title, cost);
    }

    public Product(String title, int cost) {
        this(UUID.randomUUID().toString(), title, cost);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("Price can't be negative");
        }

        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", prodId='" + prodId + '\'' +
                ", title='" + title + '\'' +
                ", cost=" + cost +
                '}';
    }
}
