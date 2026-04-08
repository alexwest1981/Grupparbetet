package Wigell.Sushi.API.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double priceSek;

    public Dish() {}; //Krav för JPA

    // Manuella Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPriceSek() { return priceSek; }
    public void setPriceSek(double priceSek) { this.priceSek = priceSek; }
}
