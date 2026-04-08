package Wigell.Sushi.API.controller;

import Wigell.Sushi.API.entity.Customer;
import Wigell.Sushi.API.entity.Dish;
import Wigell.Sushi.API.entity.Room;
import Wigell.Sushi.API.entity.Order;
import Wigell.Sushi.API.entity.Booking;
import Wigell.Sushi.API.repository.CustomerRepository;
import Wigell.Sushi.API.repository.DishRepository;
import Wigell.Sushi.API.repository.RoomRepository;
import Wigell.Sushi.API.repository.OrderRepository;
import Wigell.Sushi.API.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
// import java.util.Optional; // Removed unused import

@RestController
@RequestMapping("/api/v1")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final CustomerRepository customerRepository;
    private final DishRepository dishRepository;
    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
    private final BookingRepository bookingRepository;

    public AdminController(CustomerRepository customerRepository,
                           DishRepository dishRepository,
                           RoomRepository roomRepository,
                           OrderRepository orderRepository,
                           BookingRepository bookingRepository) {
        this.customerRepository = customerRepository;
        this.dishRepository = dishRepository;
        this.roomRepository = roomRepository;
        this.orderRepository = orderRepository;
        this.bookingRepository = bookingRepository;
    }

    // --- Customer Endpoints ---

    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        logger.info("Admin requested all customers.");
        return customerRepository.findAll();
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Admin created customer {}", savedCustomer.getUsername());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCustomer.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedCustomer);
    }

    @PutMapping("/customers/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long customerId, @RequestBody Customer customerDetails) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setName(customerDetails.getName());
                    customer.setAddress(customerDetails.getAddress());
                    Customer updated = customerRepository.save(customer);
                    logger.info("Admin updated customer ID: {} ({})", customerId, updated.getUsername());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            logger.info("Admin deleted customer ID: {}", customerId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/customers/{customerId}/addresses")
    public ResponseEntity<Customer> addAddress(@PathVariable Long customerId, @RequestBody String address) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setAddress(address);
                    customerRepository.save(customer);
                    logger.info("Admin added/updated address for customer ID: {}", customerId);
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/customers/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long customerId, @PathVariable Long addressId) {
        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setAddress(null);
                    customerRepository.save(customer);
                    logger.info("Admin deleted address for customer {} (addressId: {})", customerId, addressId);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // --- Dish Endpoints ---

    @GetMapping("/dishes")
    public List<Dish> getAllDishes() {
        logger.info("Admin requested all dishes.");
        return dishRepository.findAll();
    }

    @GetMapping("/dishes/{dishId}")
    public ResponseEntity<Dish> getDishById(@PathVariable Long dishId) {
        logger.info("Admin requested dish with ID: {}", dishId);
        return dishRepository.findById(dishId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/dishes")
    public ResponseEntity<Dish> addDish(@RequestBody Dish dish) {
        Dish savedDish = dishRepository.save(dish);
        logger.info("Admin created dish: {}", savedDish.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedDish.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedDish);
    }

    @PutMapping("/dishes/{dishId}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long dishId, @RequestBody Dish dishDetails) {
        return dishRepository.findById(dishId)
                .map(dish -> {
                    dish.setName(dishDetails.getName());
                    dish.setPriceSek(dishDetails.getPriceSek());
                    Dish updated = dishRepository.save(dish);
                    logger.info("Admin updated dish ID: {} ({})", dishId, updated.getName());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/dishes/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long dishId) {
        if (dishRepository.existsById(dishId)) {
            dishRepository.deleteById(dishId);
            logger.info("Admin deleted dish ID: {}", dishId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Room Endpoints ---

    @GetMapping("/rooms")
    public List<Room> getAllRooms() {
        logger.info("Admin requested all rooms.");
        return roomRepository.findAll();
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long roomId) {
        logger.info("Admin requested room with ID: {}", roomId);
        return roomRepository.findById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room savedRoom = roomRepository.save(room);
        logger.info("Admin created room: {}", savedRoom.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRoom.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedRoom);
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long roomId, @RequestBody Room roomDetails) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    room.setName(roomDetails.getName());
                    room.setMaxGuests(roomDetails.getMaxGuests());
                    room.setTechnicalEquipment(roomDetails.getTechnicalEquipment());
                    Room updated = roomRepository.save(room);
                    logger.info("Admin updated room ID: {} ({})", roomId, updated.getName());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        if (roomRepository.existsById(roomId)) {
            roomRepository.deleteById(roomId);
            logger.info("Admin deleted room ID: {}", roomId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Order Endpoints ---

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        logger.info("Admin requested all orders.");
        return orderRepository.findAll();
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        logger.info("Admin requested order with ID: {}", orderId);
        return orderRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Booking Endpoints (Admin) ---

    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        logger.info("Admin requested all bookings.");
        return bookingRepository.findAll();
    }
}
