package console;

import db.Product;
import db.ProductDAO;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@FunctionalInterface
interface Command {
    void apply(Client client, Scanner line);
}

public class Client {
    private Scanner in;
    private PrintStream out;
    private ProductDAO dao;

    private final static Map<String, Command> commands = Map.of(
            "/add", Client::add,
            "/delete", Client::delete,
            "/show_all", Client::showAll,
            "/price", Client::showPrice,
            "/change_price", Client::updatePrice,
            "/filter_by_price", Client::showFiltered
    );

    public Client(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.out = out;
    }

    public void start() {
        try {
            login();
            reset();
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }

        out.println("Client is up. You can enter commands.");
        while (in.hasNextLine()) {
            execute(in.nextLine());
        }
    }

    public void execute(String command) {
        Scanner line = new Scanner(command);

        if (line.hasNext()) {
            try {
                commands.getOrDefault(line.next(), (c, sc) -> out.println("No such command")).apply(this, line);
            } catch (IllegalArgumentException e) {
                out.println(e.getMessage());
            }

            if (line.hasNextLine()) {
                out.println("Redundant arguments: " + line.nextLine().strip());
            }
        }
    }

    private void add(Scanner line) {
        String name = extractName(line);
        int cost = extractCost(line);

        dao.add(new Product(name, cost));

        out.println("Product successfully added.");
    }

    private void delete(Scanner line) {
        dao.delete(extractName(line));

        out.println("Product successfully deleted.");
    }

    private void showAll(Scanner line) {
        dao.list().forEach(out::println);
    }

    private void showPrice(Scanner line) {
        out.println("Price: " + dao.getByName(extractName(line)).getCost());
    }

    private void updatePrice(Scanner line) {
        String name = extractName(line);
        int cost = extractCost(line);

        dao.updatePrice(name, cost);

        out.println("Price successfully updated.");
    }

    private void showFiltered(Scanner line) {
        int costFrom  = extractCost(line);
        int costTo = extractCost(line);

        dao.listFromPriceRange(costFrom, costTo).forEach(out::println);
    }

    private void login() {
        out.println("Enter login and password:");
        String username = in.next();
        String password = in.next();
        in.nextLine();

        dao = new ProductDAO(username, password);
        System.out.println("Login successful");
    }

    private void reset() {
        out.println("Enter number of auto-generated products:");
        int n = in.nextInt();
        List<Product> list = new ArrayList<>();
        for (int i = 1; i < n + 1; i++) {
            list.add(new Product("товар" + i, i * 100));
        }
        dao.clear();
        dao.add(list);

        out.println("Table successfully filled.");
    }

    private int extractCost(Scanner line) {
        if (!line.hasNextInt()) {
            throw new IllegalArgumentException("No product price provided!");
        }
        return line.nextInt();
    }

    private String extractName(Scanner line) {
        if (!line.hasNext()) {
            throw new IllegalArgumentException("No product name provided");
        }
        return line.next();
    }
}
