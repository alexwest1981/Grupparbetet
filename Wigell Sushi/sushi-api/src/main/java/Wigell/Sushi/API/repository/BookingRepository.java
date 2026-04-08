package Wigell.Sushi.API.repository;

import Wigell.Sushi.API.entity.Booking;
import Wigell.Sushi.API.entity.Customer; // Import Customer entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByCustomer(Customer customer); // Added this method
}
