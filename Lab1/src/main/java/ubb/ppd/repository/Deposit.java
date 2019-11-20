package ubb.ppd.repository;

import lombok.Getter;
import ubb.ppd.domain.Product;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class Deposit {

    private Map<Product, Integer> storedProducts;

    public Deposit() {
        this.storedProducts = new HashMap<>();
    }

    public void add(Product product, int quantity) {

        if (this.storedProducts.containsKey(product)) {
            this.storedProducts.replace(product, this.storedProducts.get(product) + quantity);
        } else {
            this.storedProducts.put(product, quantity);
        }
    }

    public Set<Product> getStoredProducts() {
        return this.storedProducts.keySet();
    }

    public Integer getQuantityOfroduct(Product product) {
        return this.storedProducts.getOrDefault(product, 0);
    }

    public void remove(Product product, int quantity) {

        if (this.storedProducts.containsKey(product)) {
            if (this.getQuantityOfroduct(product) == 0) {
                throw new RuntimeException("No more items of this type!!");
            } else if (this.getQuantityOfroduct(product) - quantity < 0)
            {
                throw new RuntimeException("There are not enough products!");
            }
            else
            {
                this.storedProducts.replace(product, this.storedProducts.get(product) - quantity);
            }
        } else {
            throw new RuntimeException("There is no such product in the deposit!");
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        for (Product product: this.storedProducts.keySet()) {
            result
                    .append("We have in the deposit the product ")
                    .append(product.getType())
                    .append(" which costs ")
                    .append(product.getPrice())
                    .append(" in the quantity of: ")
                    .append(this.getQuantityOfroduct(product))
                    .append("\n");
        }
        return result.toString();
    }
}