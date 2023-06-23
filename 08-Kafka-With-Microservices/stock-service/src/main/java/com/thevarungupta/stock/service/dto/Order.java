package com.thevarungupta.stock.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String name;
    private int qty;
    private double price;
}
