package Wigell.Sushi.API.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unikt ID

    @Column(unique = true, nullable = false)
    private String username; // Användarnamn

    private String name; // Kunduppgifter
    private String address; // Adress

    // Tom konstruktor krävs för JPA
    public Customer() {}

    // Konstruktor för att underlätta skapandet
    public Customer(String username, String name, String address) {
        this.username = username;
        this.name = name;
        this.address = address;
    }

    // Manuella Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
