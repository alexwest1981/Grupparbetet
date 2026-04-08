package Wigell.Sushi.API.controller;

import Wigell.Sushi.API.entity.Booking;
import Wigell.Sushi.API.entity.Order;
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

    public CustomerController(BookingRepository bookingRepository,
                              OrderRepository orderRepository,
                              CustomerRepository customerRepository,
                              RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
    }


    // Beställ takeaway POST /api/v1/orders
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // Logik för att spara ordern och dess rätter
        if (order.getOrderDishes() != null) {
            order.getOrderDishes().forEach(od -> od.setOrder(order));
        }
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
        if (booking.getBookingDishes() != null) {
            booking.getBookingDishes().forEach(bd -> bd.setBooking(booking));
        }
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

        // Uppdatera endast tillåtna fält enligt kravspecifikationen: 
        // datum, antal rätter, lokal, önskad förtäring, teknisk utrustning
        
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
        if (bookingUpdates.getBookingDishes() != null && !bookingUpdates.getBookingDishes().isEmpty()) {
            // För enkelhetens skull ersätter vi hela listan
            existingBooking.getBookingDishes().clear();
            bookingUpdates.getBookingDishes().forEach(bd -> {
                bd.setBooking(existingBooking);
                existingBooking.getBookingDishes().add(bd);
            });
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
