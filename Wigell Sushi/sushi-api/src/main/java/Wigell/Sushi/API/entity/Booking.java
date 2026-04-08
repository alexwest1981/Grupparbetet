package Wigell.Sushi.API.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List; // Added for dishes

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private int numberOfGuests; // Antalet gäster

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; // Lokal

    private String technicalEquipment; // Krav-fält: teknisk utrustning

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingDish> BookingDishes; // Önskad förtäring (maträtter och antal)

    private LocalDateTime bookingDate;

    private double totalPriceSek;
    private double totalPriceJpy;

    public Booking() {}

    // Manuella Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public String getTechnicalEquipment() { return technicalEquipment; }
    public void setTechnicalEquipment(String technicalEquipment) { this.technicalEquipment = technicalEquipment; }
    public List<BookingDish> getBookingDishes() { return BookingDishes; }
    public void setBookingDishes(List<BookingDish> BookingDishes) { this.BookingDishes = BookingDishes; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public double getTotalPriceSek() { return totalPriceSek; }
    public void setTotalPriceSek(double totalPriceSek) { this.totalPriceSek = totalPriceSek; }
    public double getTotalPriceJpy() { return totalPriceJpy; }
    public void setTotalPriceJpy(double totalPriceJpy) { this.totalPriceJpy = totalPriceJpy; }
}
