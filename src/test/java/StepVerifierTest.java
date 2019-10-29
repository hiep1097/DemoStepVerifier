import org.junit.jupiter.api.*;
import reactor.core.Fuseable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class StepVerifierTest {

    //test for create(Publisher<? extends T> publisher, long n)
    //sẽ bị lỗi do ban đầu ta chỉ request 1 phần tử nhưng lại mong đợi 3 phần tử
    @Test
    public static void createWithDefaultRequest(){
        Flux<String> flux = Flux.just("foo", "bar", "baz");
        StepVerifier.create(flux,1)
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    //test for log()
    //in ra các thông tin chi tiết
    @Test
    public static void log(){
        Flux<String> flux = Flux.just("foo", "bar", "baz");
        StepVerifier.create(flux.log())
                .expectNextCount(3)
                .verifyComplete();
    }

    //test for resetDefaultTimeout()
    //sẽ pass do thời gian xác minh là không giới hạn
    @Test
    public static void testResetDefaultTimeout(){
        StepVerifier.resetDefaultTimeout();
        StepVerifier.create(Mono.delay(Duration.ofMillis(150)))
                .expectNext(0L)
                .expectComplete()
                .verify();
    }

    //test for setDefaultTimeout(Duration timeout)
    //sẽ fail do thời gian chờ xác minh chỉ là 100 ms nhưng tạo mono mất 150 ms
    @Test
    public static void testSetDefaultTimeout(){
        StepVerifier.setDefaultTimeout(Duration.ofMillis(100));
        StepVerifier.create(Mono.delay(Duration.ofMillis(150)))
                .expectNext(0L)
                .expectComplete()
                .verify();
    }

    //test for verify(Duration duration)
    //sẽ fail do thời gian chờ xác minh chỉ là 100 ms nhưng tạo mono mất 150 ms
    @Test
    public static void testVerifyWithDuration(){
        StepVerifier.create(Mono.delay(Duration.ofMillis(150)))
                .expectNext(0L)
                .expectComplete()
                .verify(Duration.ofMillis(100));
    }

    //test for verifyLater()
    //verifyLater() trả về StepVerifier và ta có thể verify() nó sau đó
    @Test
    public static void verifyLater(){
        Flux<String> flux = Flux.just("foo", "bar", "baz");

        StepVerifier deferred1 = StepVerifier.create(flux)
                .expectNextCount(3)
                .expectComplete()
                .verifyLater();
        deferred1.verify();
    }

    //test for as(String description)
    //bước expectNext thứ 2 bị lỗi, sử dụng as sẽ mô tả chi tiết hơn trong thông báo lỗi, ta có thể xác định lỗi xảy ra ở "second"
    @Test
    public static void as(){
        Flux<String> flux = Flux.just("foo", "bar");

        StepVerifier.create(flux)
                .expectNext("foo")
                .as("first")
                .expectNext("foo")
                .as("second")
                .expectComplete()
                .verify();
    }

    //test for assertNext(Consumer<? super T> assertionConsumer)
    @Test
    public static void assertNext(){
        Flux<String> flux = Flux.just("foo");

        StepVerifier.create(flux)
                .assertNext(s -> assertThat(s).endsWith("oo"))
                .expectComplete()
                .verify();
    }

    //test for consumeNextWith(Consumer<? super T> consumer)
    //ngoại lệ AssertionError ném bởi consumer đã được ném lại khi verify()
    @Test
    public static void consumeNextWith(){
        Flux<String> flux = Flux.just("foo");

        StepVerifier.create(flux)
                .consumeNextWith(s -> {
                    if (!"bar".equals(s)) {
                        throw new AssertionError("e:" + s);
                    }
                })
                .expectComplete()
                .verify();
    }

    //test for consumeSubscriptionWith(Consumer<? super Subscription> consumer)
    @Test
    public static void consumeSubscriptionWith(){
        Mono<String> flux = Mono.just("foo");

        StepVerifier.create(flux)
                .consumeSubscriptionWith(s -> assertThat(s).isInstanceOf(Fuseable.QueueSubscription.class))
                .expectNext("foo")
                .expectComplete()
                .verify();
    }

    //test for expectNext(T t)
    @Test
    public static void expectNext(){
        Mono<String> flux = Mono.just("foo");

        StepVerifier.create(flux)
                .expectNext("foo")
                .expectComplete()
                .verify();
    }

    //test for expectNext(T... ts)
    @Test
    public static void expectNextMoreThan6(){
        Flux<Integer> flux = Flux.range(1,7);

        StepVerifier.create(flux)
                .expectNext(1,2,3,4,5,6,7)
                .expectComplete()
                .verify();
    }

    //test for expectNextCount(long count)
    @Test
    public static void expectNextCount(){
        Flux<Integer> flux = Flux.range(1,7);

        StepVerifier.create(flux)
                .expectNext(1)
                .expectNextCount(5)
                .expectNext(7)
                .expectComplete()
                .verify();
    }

    //test for expectNextMatches(Predicate<? super T> predicate)
    //phần tử đầu tiên là "foo" khớp với predicate truyền vào
    @Test
    public static void expectNextMatches(){
        Flux<String> flux = Flux.just("foo", "bar", "baz");

        StepVerifier.create(flux)
                .expectNextMatches(s->s.startsWith("fo"))
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    //test for expectNextSequence(Iterable<? extends T> iterable)
    @Test
    public static void expectNextSequence(){
        Flux<String> flux = Flux.just("foo", "bar", "foobar");

        StepVerifier.create(flux)
                .expectNextSequence(Arrays.asList("foo", "bar", "foobar"))
                .expectComplete()
                .verify();
    }

    //test for recordWith(Supplier<? extends Collection<T>> supplier)
    @Test
    public static void recordWith(){
        Flux<String> flux = Flux.just("foo", "bar", "foobar");

        ArrayList<String> list = new ArrayList<>();

        StepVerifier.create(flux)
                .recordWith(()->list)
                .expectNextCount(3)
                .expectComplete()
                .verify();

        System.out.println(list);
    }

    //test for expectRecordedMatches(Predicate<? super Collection<T>> predicate)
    @Test
    public static void expectRecordedMatches(){
        Flux<String> flux = Flux.just("foo", "bar", "foobar");

        StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .expectNextCount(3)
                .expectRecordedMatches(c -> c.contains("foobar"))
                .expectComplete()
                .verify();
    }

    //test for consumeRecordedWith(Consumer<? super Collection<T>> consumer)
    @Test
    public static void consumeRecordedWith(){
        final List<Integer> source = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Flux<Integer> flux = Flux.fromStream(source.stream());

        StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .expectNextCount(10)
                .consumeRecordedWith(c -> assertThat(c).containsExactlyElementsOf(source))
                .expectComplete()
                .verify();
    }

    //test for thenConsumeWhile(Predicate<T> predicate)
    @Test
    public static void thenConsumeWhile(){

        StepVerifier.create(Flux.range(3, 8))
                .expectNextMatches(first -> first == 3)
                .thenConsumeWhile(v -> v < 9)
                .expectNext(9)
                .expectNext(10)
                .expectComplete()
                .log()
                .verify();
    }

    //test for thenConsumeWhile(Predicate<T> predicate, Consumer<T> consumer)
    @Test
    public static void thenConsumeWhileWithConsumer(){
        LongAdder count = new LongAdder();

        StepVerifier.create(Flux.range(3, 8))
                .expectNextMatches(first -> first == 3)
                .thenConsumeWhile(v -> v < 9, v -> count.increment())
                .expectNext(9)
                .expectNext(10)
                .expectComplete()
                .log()
                .verify();

        assertThat(count.intValue()).isEqualTo(5);
    }

    //test for thenRequest(long n)
    //ban đầu request 2 phần tử khi khởi tạo StepVerifier, sau đó sẽ request thêm 1 phần tử khi gọi thenRequest(1)
    @Test
    public static void thenRequest(){

        StepVerifier.create(Flux.just("foo","bar","baz"),2)
                .expectNextCount(2)
                .thenRequest(1)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    //test for consumeErrorWith(Consumer<Throwable> consumer)
    @Test
    public static void consumeErrorWith(){
        Flux<String> flux = Flux.just("foo")
                .concatWith(Mono.error(new IllegalArgumentException()));

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> StepVerifier.create(flux)
                        .expectNext("foo")
                        .consumeErrorWith(throwable -> {
                            if (!(throwable instanceof IllegalStateException)) {
                                throw new AssertionError(throwable.getClass()
                                        .getSimpleName());
                            }
                        })
                        .verify())
                .withMessage("IllegalArgumentException");
    }

    //test for thenCancel()
    @Test
    public static void thenCancel(){
        Flux<String> flux = Flux.just("foo", "bar", "baz");

        StepVerifier.create(flux)
                .expectNext("foo")
                .thenCancel()
                .verify();
    }

    //test for verifyError()
    @Test
    public static void verifyError(){
        StepVerifier.create(Flux.error(new IllegalArgumentException()))
                .verifyError();
    }

    //test for verifyError(Class<? extends Throwable> clazz)
    @Test
    public static void verifyErrorClass(){
        StepVerifier.create(Flux.error(new IllegalArgumentException()))
                .verifyError(IllegalArgumentException.class);
    }

    //test for verifyErrorMessage(String errorMessage)
    @Test
    public static void verifyErrorMessage(){
        StepVerifier.create(Flux.error(new IllegalArgumentException("boom")))
                .verifyErrorMessage("boom");
    }

    //test for verifyErrorMatches(Predicate<Throwable> predicate)
    @Test
    public static void verifyErrorMatches(){
        StepVerifier.create(Flux.error(new IllegalArgumentException("boom")))
                .verifyErrorMatches(e -> e instanceof IllegalArgumentException);
    }

    //test for verifyErrorSatisfies(Consumer<Throwable> assertionConsumer)
    @Test
    public static void verifyErrorSatisfies(){
        StepVerifier.create(Flux.error(new IllegalArgumentException("boom")))
                .verifyErrorSatisfies(e -> assertThat(e).hasMessage("boom"));
    }

    //test for
    //hasDiscarded(Object... values)
    //hasDiscardedElements()
    //hasDiscardedElementsMatching(Predicate<Collection<Object>> matcher)
    //hasDiscardedElementsSatisfying(Consumer<Collection<Object>> consumer)
    //hasDiscardedExactly(Object... values)

    @Test
    public static void testDiscarded(){
        StepVerifier.create(Flux.just(1, 2, 3).filter(i -> i == 2))
                .expectNext(2)
                .expectComplete()
                .verifyThenAssertThat()
                .hasDiscardedElements()
                .hasDiscardedExactly(1, 3)
                .hasDiscarded(1)
                .hasDiscardedElementsMatching(list -> list.stream().allMatch(e -> (int)e % 2 != 0))
                .hasDiscardedElementsSatisfying(list -> assertThat(list).containsOnly(1, 3));
    }
    //test for
    //hasDropped(Object... values)
    //hasDroppedElements()
    //hasDroppedExactly(Object... values)

    @Test
    public static void testDropped(){
        StepVerifier.create(Flux.from(s -> {
            s.onSubscribe(Operators.emptySubscription());
            s.onNext("foo");
            s.onComplete();
            s.onNext("bar");
            s.onNext("baz");
        }).take(3))
                .expectNext("foo")
                .expectComplete()
                .verifyThenAssertThat()
                .hasDroppedElements()
                .hasDropped("baz")
                .hasDroppedExactly("baz", "bar");
    }

    //test for
    //hasDroppedErrors()
    //hasDroppedErrors(int n)
    //hasDroppedErrorMatching(Predicate<Throwable> matcher)
    //hasDroppedErrorOfType(Class<? extends Throwable> clazz)
    //hasDroppedErrorWithMessage(String message)
    //hasDroppedErrorWithMessageContaining(String messagePart)
    //hasDroppedErrorsSatisfying(Consumer<Collection<Throwable>> errorsConsumer)
    @Test
    public static void testDroppedError(){
        Throwable err1 = new IllegalStateException("boom1");
        Throwable err2 = new IllegalStateException("boom2");
        StepVerifier.create(Flux.from(s -> {
            s.onSubscribe(Operators.emptySubscription());
            s.onError(err1);
            s.onError(err2);
        }).buffer(1))
                .expectError()
                .verifyThenAssertThat()
                .hasDroppedErrors()
                .hasDroppedErrors(1)
                .hasDroppedErrorOfType(IllegalStateException.class)
                .hasDroppedErrorWithMessageContaining("boom")
                .hasDroppedErrorWithMessage("boom2")
                .hasDroppedErrorMatching(t -> t instanceof IllegalStateException && "boom2".equals(t.getMessage()))
                .hasDroppedErrorsSatisfying(c -> assertThat(c).hasSize(1));;
    }

    //test for
    //hasOperatorErrors()
    //hasOperatorErrors(int n)
    //hasOperatorErrorOfType(Class<? extends Throwable> clazz)
    //hasOperatorErrorWithMessage(String message)
    //hasOperatorErrorWithMessageContaining(String messagePart)
    //hasOperatorErrorMatching(Predicate<Throwable> matcher)
    //hasOperatorErrorsSatisfying(Consumer<Collection<Tuple2<Optional<Throwable>,Optional<?>>>> errorsConsumer)
    @Test
    public static void testOperatorErrors(){
        IllegalStateException err1 = new IllegalStateException("boom1");
        StepVerifier.create(Flux.just("test").map(d -> {
            throw err1;
        }))
                .expectError()
                .verifyThenAssertThat()
                .hasOperatorErrors()
                .hasOperatorErrors(1)
                .hasOperatorErrorOfType(IllegalStateException.class)
                .hasOperatorErrorWithMessageContaining("boom")
                .hasOperatorErrorWithMessage("boom1")
                .hasOperatorErrorMatching(t -> t instanceof IllegalStateException && "boom1".equals(t.getMessage()))
                .hasOperatorErrorsSatisfying(c -> assertThat(c).hasSize(1));

    }

    //test for hasNotDiscardedElements()
    @Test
    public static void hasNotDiscardedElements(){
        StepVerifier.create(Mono.empty())
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDiscardedElements();
    }

    //test for hasNotDroppedElements()
    @Test
    public static void hasNotDroppedElements(){
        StepVerifier.create(Mono.empty())
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedElements();
    }

    //test for hasNotDroppedErrors()
    @Test
    public static void hasNotDroppedErrors(){
        StepVerifier.create(Mono.empty())
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    //test for tookLessThan(Duration d)
    @Test
    public static void tookLessThan(){
        StepVerifier.create(Mono.delay(Duration.ofMillis(500)).then())
                .expectComplete()
                .verifyThenAssertThat()
                .tookLessThan(Duration.ofSeconds(1));
    }

    //test for tookMoreThan(Duration d)
    @Test
    public static void tookMoreThan(){
        StepVerifier.create(Mono.delay(Duration.ofMillis(500)).then())
                .expectComplete()
                .verifyThenAssertThat()
                .tookMoreThan(Duration.ofMillis(100));
    }

    public static void main(String[] args) {
        testOperatorErrors();
    }

}
