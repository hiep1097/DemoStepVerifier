package hello;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {
    @Autowired
    CustomerRepository repository;

    public void init(){
        for (int i=0;i<100;i++){
            repository.save(new Customer("Firstname "+i, "Lastname "+i)).block();
        }
    }

    @Before
    public void setUp() throws Exception {
        repository.deleteAll().block();
        init();
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testFindAll() {
        StepVerifier.create(repository.findAll())
                .expectNextCount(100)
                .expectComplete()
                .verify();
    }

    @Test
    public void testFindByFirstName() {
        StepVerifier.create(repository.findByFirstName("Firstname 0"))
                .expectNextMatches(c->c.firstName.equals("Firstname 0") && c.lastName.equals("Lastname 0"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testFindByLastName() {
        StepVerifier.create(repository.findByLastName("Lastname 1"))
                .expectNextMatches(c->c.firstName.equals("Firstname 1") && c.lastName.equals("Lastname 1"))
                .expectComplete()
                .verify();
    }
}