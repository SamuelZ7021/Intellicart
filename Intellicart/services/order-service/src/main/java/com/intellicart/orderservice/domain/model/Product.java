package com.intellicart.orderservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter
public class Product {
    @Id
    private Long id; // El ID vendrá del catálogo o servicio de productos
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
}