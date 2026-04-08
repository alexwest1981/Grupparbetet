package Wigell.Sushi.API.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer; // Kunduppgifter

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDish> orderDishes; // Rätter (maträtter och antal)

    private double totalPriceSek;
    private double totalPriceJpy; // Priset ska presenteras i SEK och JPY

    public Order() {}

    // Manuella Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public List<OrderDish> getOrderDishes() { return orderDishes; }
    public void setOrderDishes(List<OrderDish> orderDishes) { this.orderDishes = orderDishes; }
    public double getTotalPriceSek() { return totalPriceSek; }
    public void setTotalPriceSek(double totalPriceSek) { this.totalPriceSek = totalPriceSek; }
    public double getTotalPriceJpy() { return totalPriceJpy; }
    public void setTotalPriceJpy(double totalPriceJpy) { this.totalPriceJpy = totalPriceJpy; }
}