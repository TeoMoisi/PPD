package ubb.ppd.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class Product {

    private String type;
    private float price;

    public Product(String type, float price) {
        this.type = type;
        this.price = price;
    }
}