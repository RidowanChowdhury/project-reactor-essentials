import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Slf4j
public class FluxTest {


    @Test
    public void test() {
        log.info("test!!");
    }

    @Test
    public void fluxSubscriber() {
        Flux<String> stringFlux = Flux.just("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial").log();
        Flux<String> stringFlux1 = Flux.fromIterable(List.of("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial"));
        stringFlux.subscribe(a -> log.info(a), Throwable::printStackTrace, () -> log.info("Done"),
                subscription -> subscription.request(2)); //bounded subscription;
        StepVerifier.create(stringFlux)
                .expectNext("Ridwan")
                .expectNext("Bruno")
                .expectNext("Sancho")
                .expectNext("Ericsen")
                .expectNext("Martial")
                .expectComplete()
                .verify();
        StepVerifier.create(stringFlux1)
                .expectNext("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial")
                .verifyComplete();

        // TODO: 8/8/22 could not test with bounded publisher. 
    }

    @Test
    public void baseSubscriberTest() {
        Flux<String> stringFlux = Flux.fromIterable(List.of("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial")).log();

        stringFlux.subscribe(new BaseSubscriber<String>() {
            final int request_count = 2;
            int count = 0;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(request_count);
            }

            @Override
            protected void hookOnNext(String value) {
                count++;
                if (count >= request_count) {
                    count = 0;
                    request(request_count);
                }
            }
        });
    }

    @Test
    public void fluxSuscriberBackpressure() {
        Flux<String> stringFlux = Flux.fromIterable(List.of("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial"))
                .log()
                .limitRate(2);
        Flux<String> stringFlux1 = Flux.fromIterable(List.of("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial"))
                .log()
                .limitRequest(2);

        StepVerifier.create(stringFlux)
                .expectNext("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial")
                .verifyComplete();
        StepVerifier.create(stringFlux1)
                .expectNext("Ridwan", "Bruno")
                .verifyComplete();
// TODO: 8/20/22 limitrate need to be the last operator. Need investigation on limitrate with hightide, lowtide
    }

    @Test
    public void connectableFluxTest() {
        ConnectableFlux<String> stringFlux = Flux.fromIterable(List.of("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial"))
                .log().
                delayElements(Duration.ofMillis(100))
                .publish();
        Flux<String> stringFlux1 = Flux.fromIterable(List.of("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial"))
                .log().
                delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);

        StepVerifier.create(stringFlux)
                .then(stringFlux::connect)
                .expectNext("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial")
                .verifyComplete();

        StepVerifier.create(stringFlux1)
                .then(stringFlux1::subscribe)
                .expectNext("Ridwan", "Bruno", "Sancho", "Ericsen", "Martial")
                .verifyComplete();

// TODO: 8/20/22 Need investigation on its use cases
    }

    @Test
    public void IOtest() throws Exception {
        Mono<List<String>> listMono = Mono.fromCallable(() -> Files.readAllLines(Path.of("aa.txt")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());
        Thread.sleep(10000);

        listMono.subscribe(a -> log.info("data is {}", a));

    }

    @Test
    public void deferTest() {
        Mono<Long> stringMono = Mono.defer(() -> Mono.just(System.currentTimeMillis())).log();

        stringMono.subscribe();
        stringMono.subscribe();

        // TODO: 8/26/22 Defer ensures that publisher instantiates when subscriber subscribes.

    }

    @Test
    public void mergeAndConcatTest() throws Exception {
        Flux<String> a = Flux.just("A", "B").delayElements(Duration.ofMillis(200));
        Flux<String> b = Flux.just("C", "D");

        Flux<String> merged = Flux.merge(a, b, a).log();
        Flux<String> concated = Flux.concat(a, b, a).log();

        merged.subscribe();
        concated.subscribe();
        Thread.sleep(2000);

        // TODO: 8/26/22 merge works eagerly, but concat doesn't.

    }

    @Test
    public void zipTest() throws Exception {
        Flux<String> a = Flux.just("A", "B").delayElements(Duration.ofMillis(200));;
        Flux<String> b = Flux.just("C", "D", "E");

        Flux<String> zipped = Flux.zip(a, b)
                .flatMap(objects -> Flux.just(String.join("",objects.getT1(),objects.getT2()))).log();

        zipped.subscribe();
        Thread.sleep(2000);

    }
}
