package Wigell.Sushi.API.controller;

import Wigell.Sushi.API.entity.Booking;
import Wigell.Sushi.API.entity.BookingDish;
import Wigell.Sushi.API.entity.Dish;
import Wigell.Sushi.API.entity.Order;
import Wigell.Sushi.API.entity.OrderDish;
import Wigell.Sushi.API.entity.Customer;
import Wigell.Sushi.API.exception.ResourceNotFoundException;
import Wigell.Sushi.API.repository.BookingRepository;
import Wigell.Sushi.API.repository.DishRepository;
import Wigell.Sushi.API.repository.OrderRepository;
import Wigell.Sushi.API.repository.CustomerRepository;
import Wigell.Sushi.API.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final DishRepository dishRepository;

    public CustomerController(BookingRepository bookingRepository,
                              OrderRepository orderRepository,
                              CustomerRepository customerRepository,
                              RoomRepository roomRepository,
                              DishRepository dishRepository) {
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.dishRepository = dishRepository;
    }


    // Beställ takeaway POST /api/v1/orders
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        double totalSek = 0;

        if (order.getOrderDishes() != null) {
            for (OrderDish od : order.getOrderDishes()) {
                od.setOrder(order);
                if (od.getDish() != null && od.getDish().getId() != null) {
                    Dish dish = dishRepository.findById(od.getDish().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Hittade ingen maträtt med ID " + od.getDish().getId()));
                    totalSek += dish.getPriceSek() * od.getQuantity();
                    od.setDish(dish); // Sätt den riktiga entiteten från databasen
                }
            }
        }
        order.setTotalPriceSek(totalSek);

        // --- EXTERNT ANROP TILL VALUTATJÄNSTEN (VG-krav) ---
        try {
            RestTemplate restTemplate = new RestTemplate();
            // OBS! ÄNDRA 8081 TILL DEN PORT KRISTINA ANVÄNDER, OCH ÄNDRA URL:EN SÅ DEN MATCHAR HENNES API!
            String currencyApiUrl = "http://localhost:8081/api/convert?amount=" + totalSek + "&from=SEK&to=JPY";
            
            // Antar att hennes API returnerar en ren siffra (Double). Om det är JSON måste detta ändras till en Map eller DTO.
            Double convertedPriceJpy = restTemplate.getForObject(currencyApiUrl, Double.class);
            if (convertedPriceJpy != null) {
                order.setTotalPriceJpy(convertedPriceJpy);
                logger.info("Valutakonvertering lyckades: {} SEK blev {} JPY", totalSek, convertedPriceJpy);
            }
        } catch (Exception e) {
            logger.error("Kunde inte nå valutatjänsten. Sätter JPY-priset till 0. Fel: {}", e.getMessage());
            order.setTotalPriceJpy(0.0);
        }
        // ---------------------------------------------------

        Order savedOrder = orderRepository.save(order);
        logger.info("Customer {} created order {}", 
            savedOrder.getCustomer() != null ? savedOrder.getCustomer().getUsername() : "unknown", 
            savedOrder.getId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedOrder);
    }

    // Reservera lokal POST /api/v1/bookings
    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        double totalSek = 0;

        if (booking.getBookingDishes() != null) {
            for (BookingDish bd : booking.getBookingDishes()) {
                bd.setBooking(booking);
                if (bd.getDish() != null && bd.getDish().getId() != null) {
                    Dish dish = dishRepository.findById(bd.getDish().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Hittade ingen maträtt med ID " + bd.getDish().getId()));
                    totalSek += dish.getPriceSek() * bd.getQuantity();
                    bd.setDish(dish);
                }
            }
        }
        booking.setTotalPriceSek(totalSek);

        // --- EXTERNT ANROP TILL VALUTATJÄNSTEN (VG-krav) ---
        try {
            RestTemplate restTemplate = new RestTemplate();
            // OBS! ÄNDRA 8081 TILL DEN PORT KRISTINA ANVÄNDER, OCH ÄNDRA URL:EN SÅ DEN MATCHAR HENNES API!
            String currencyApiUrl = "http://localhost:8081/api/convert?amount=" + totalSek + "&from=SEK&to=JPY";
            
            Double convertedPriceJpy = restTemplate.getForObject(currencyApiUrl, Double.class);
            if (convertedPriceJpy != null) {
                booking.setTotalPriceJpy(convertedPriceJpy);
                logger.info("Valutakonvertering lyckades: {} SEK blev {} JPY", totalSek, convertedPriceJpy);
            }
        } catch (Exception e) {
            logger.error("Kunde inte nå valutatjänsten. Sätter JPY-priset till 0. Fel: {}", e.getMessage());
            booking.setTotalPriceJpy(0.0);
        }
        // ---------------------------------------------------

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Customer {} created booking {}", 
            savedBooking.getCustomer() != null ? savedBooking.getCustomer().getUsername() : "unknown", 
            savedBooking.getId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBooking.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedBooking);
    }

    @PatchMapping("/bookings/{bookingId}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long bookingId, @RequestBody Booking bookingUpdates) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Hittade ingen matchande bokning med id: " + bookingId));

        if (bookingUpdates.getBookingDate() != null) {
            existingBooking.setBookingDate(bookingUpdates.getBookingDate());
        }
        if (bookingUpdates.getNumberOfGuests() > 0) {
            existingBooking.setNumberOfGuests(bookingUpdates.getNumberOfGuests());
        }
        if (bookingUpdates.getRoom() != null && bookingUpdates.getRoom().getId() != null) {
            roomRepository.findById(bookingUpdates.getRoom().getId()).ifPresent(existingBooking::setRoom);
        }
        if (bookingUpdates.getTechnicalEquipment() != null) {
            existingBooking.setTechnicalEquipment(bookingUpdates.getTechnicalEquipment());
        }
        
        // Om rätterna uppdateras måste vi räkna om priset!
        if (bookingUpdates.getBookingDishes() != null && !bookingUpdates.getBookingDishes().isEmpty()) {
            existingBooking.getBookingDishes().clear();
            double nyTotalSek = 0;
            
            for (BookingDish bd : bookingUpdates.getBookingDishes()) {
                bd.setBooking(existingBooking);
                if (bd.getDish() != null && bd.getDish().getId() != null) {
                    Dish dish = dishRepository.findById(bd.getDish().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Hittade ingen maträtt med ID " + bd.getDish().getId()));
                    nyTotalSek += dish.getPriceSek() * bd.getQuantity();
                    bd.setDish(dish);
                }
                existingBooking.getBookingDishes().add(bd);
            }
            existingBooking.setTotalPriceSek(nyTotalSek);
            
            // Gör nytt anrop till Kristina för att räkna om JPY-priset!
            try {
                RestTemplate restTemplate = new RestTemplate();
                String currencyApiUrl = "http://localhost:8081/api/convert?amount=" + nyTotalSek + "&from=SEK&to=JPY";
                Double convertedPriceJpy = restTemplate.getForObject(currencyApiUrl, Double.class);
                existingBooking.setTotalPriceJpy(convertedPriceJpy != null ? convertedPriceJpy : 0.0);
            } catch (Exception e) {
                logger.error("Kunde inte nå valutatjänsten vid uppdatering. Sätter JPY-priset till 0.");
                existingBooking.setTotalPriceJpy(0.0);
            }
        }

        Booking savedBooking = bookingRepository.save(existingBooking);
        logger.info("Customer updated booking {}. Fields: datum, gäster, lokal, förtäring, utrustning", bookingId);

        return ResponseEntity.ok(savedBooking);
    }

    // Se tidigare och aktiva bokningar GET /api/v1/bookings?customerId={customerId}
    @GetMapping(value = "/bookings", params = "customerId")
    public ResponseEntity<List<Booking>> getBookingsByCustomerId(@RequestParam Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Hittade ingen matchande kund med id: " + customerId));
        
        List<Booking> bookings = bookingRepository.findByCustomer(customer);
        logger.info("Customer requested bookings for customerId {}", customerId);
        return ResponseEntity.ok(bookings);
    }

    // Hämta beställningar GET /api/v1/orders?customerId={customerId}
    @GetMapping(value = "/orders", params = "customerId")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@RequestParam Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Hittade ingen matchande kund med id: " + customerId));
                
        List<Order> orders = orderRepository.findByCustomer(customer);
        logger.info("Customer requested orders for customerId {}", customerId);
        return ResponseEntity.ok(orders);
    }
}
