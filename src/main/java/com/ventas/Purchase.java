package com.ventas;

public class Purchase {
    int id;
    int productId;
    int quantity;
    double unitCost;
    double total;
    String date;

    public Purchase(int id, int productId, int quantity, double unitCost, double total, String date) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.total = total;
        this.date = date;
    }
}
