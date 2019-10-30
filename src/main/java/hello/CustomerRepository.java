package hello;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

@Repository
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    Flux<Customer> findByFirstName(String firstName);
    Flux<Customer> findByLastName(String lastName);

}