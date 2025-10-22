package com.ventas;

public class Product {
    public int id;
    public String code;
    public String name;
    public double price;
    public int stock;

    public Product(int id, String code, String name, double price, int stock) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
