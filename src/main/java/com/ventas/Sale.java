package com.ventas;

public class Sale {
    public int id;
    public String date;
    public int productId;
    public String productName;
    public int quantity;
    public double unitPrice;
    public double total;
    public String seller;

    public Sale(int id, String date, int productId, String productName, int quantity, double unitPrice, double total, String seller) {
        this.id = id;
        this.date = date;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
        this.seller = seller;
    }
}
