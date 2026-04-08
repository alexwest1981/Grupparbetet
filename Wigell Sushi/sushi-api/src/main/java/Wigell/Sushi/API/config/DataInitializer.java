package Wigell.Sushi.API.config;

import Wigell.Sushi.API.entity.*;
import Wigell.Sushi.API.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final DishRepository dishRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CustomerRepository customerRepository,
                           DishRepository dishRepository,
                           RoomRepository roomRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           OrderRepository orderRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.dishRepository = dishRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Skapa Användare
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ADMIN"));
            userRepository.save(new User("user", passwordEncoder.encode("user123"), "USER"));
            userRepository.save(new User("kim", passwordEncoder.encode("sushi-kim"), "USER"));
            userRepository.save(new User("alex", passwordEncoder.encode("sushi-alex"), "USER"));
            userRepository.save(new User("anna", passwordEncoder.encode("sushi-anna"), "USER"));

            // Skapa Kunder
            Customer c1 = customerRepository.save(new Customer("user", "Sven Svensson", "Storgatan 1"));
            Customer c2 = customerRepository.save(new Customer("kim", "Kim Andersson", "Lillgatan 2"));
            Customer c3 = customerRepository.save(new Customer("alex", "Alex Wigell", "Gatuvägen 3"));
            Customer c4 = customerRepository.save(new Customer("anna", "Anna Persson", "Brogatan 4"));
            Customer c5 = customerRepository.save(new Customer("bob", "Bob Dylan", "Musikvägen 5"));

            // Skapa Rätter
            Dish d1 = new Dish(); d1.setName("Lax Sushi 10st"); d1.setPriceSek(120);
            Dish d2 = new Dish(); d2.setName("Maki Rullar 8st"); d2.setPriceSek(95);
            Dish d3 = new Dish(); d3.setName("Gyoza 6st"); d3.setPriceSek(75);
            Dish d4 = new Dish(); d4.setName("Sashimi Mix"); d4.setPriceSek(150);
            Dish d5 = new Dish(); d5.setName("Yakiniku"); d5.setPriceSek(130);
            dishRepository.saveAll(List.of(d1, d2, d3, d4, d5));

            // Skapa Lokaler
            Room r1 = roomRepository.save(new Room("Lilla Rummet", 4, "TV, Whiteboard"));
            Room r2 = roomRepository.save(new Room("Stora Salen", 20, "Projektor, Ljudsystem"));
            Room r3 = roomRepository.save(new Room("VIP-loungen", 6, "Minibar"));
            Room r4 = roomRepository.save(new Room("Uterummet", 10, "Värmelampor"));
            Room r5 = roomRepository.save(new Room("Källaren", 8, "Karaoke"));

            // Skapa en bokning
            Booking b1 = new Booking();
            b1.setCustomer(c1);
            b1.setRoom(r1);
            b1.setNumberOfGuests(4);
            b1.setBookingDate(LocalDateTime.now().plusDays(1));
            b1.setTechnicalEquipment("TV");
            b1.setTotalPriceSek(500);
            b1.setTotalPriceJpy(7000);
            
            BookingDish bd1 = new BookingDish(b1, d1, 2);
            b1.setBookingDishes(new ArrayList<>(List.of(bd1)));
            bookingRepository.save(b1);

            // Skapa en order
            Order o1 = new Order();
            o1.setCustomer(c2);
            o1.setTotalPriceSek(215);
            o1.setTotalPriceJpy(3000);
            OrderDish od1 = new OrderDish(o1, d1, 1);
            OrderDish od2 = new OrderDish(o1, d2, 1);
            o1.setOrderDishes(new ArrayList<>(List.of(od1, od2)));
            orderRepository.save(o1);

            System.out.println("Data initialization complete: 5 Users/Customers, 5 Dishes, 5 Rooms created.");
        }
    }
}
