package Wigell.Sushi.API.repository;

import Wigell.Sushi.API.entity.Order;
import Wigell.Sushi.API.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
}
