import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
public class FluxTest {


    @Test
    public void test() {
        log.info("test!!");
    }

    @Test
    public void fluxSubscriber() {
        Flux<String> stringFlux = Flux.just("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial").log();

        StepVerifier.create(stringFlux)
                .expectNext("Ridwan")
                .expectNext("Bruno")
                .expectNext("Sancho")
                .expectNext("Ericsen")
                .expectNext("Martial")
                .expectComplete()
                .verify();
        StepVerifier.create(stringFlux)
                .expectNext("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial")
                .verifyComplete();

        // TODO: 8/8/22 could not test with bounded publisher. 
    }
}
