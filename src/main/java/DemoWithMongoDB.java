import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DemoWithMongoDB {
    public static void main(String[] args) {
        CodecRegistry pojoCodecRegistry = fromRegistries(com.mongodb.reactivestreams.client.MongoClients.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .build();
        MongoClient mongoClient =  MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("db1");
        MongoCollection<Person> collection = database.getCollection("mycol1",Person.class);
        SubscriberHelpers.ObservableSubscriber subscriber = new SubscriberHelpers.ObservableSubscriber<Success>();
        //collection.drop().subscribe(subscriber);
//        for (int i=0;i<100;i++) {
//            Person person = new Person("Person "+i,i);
//            collection.insertOne(person).subscribe(subscriber);
//            try {
//                subscriber.await();
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
//        }
        StepVerifier.create(Flux.from(collection.countDocuments()).log()).expectNextCount(80).expectComplete().verify();

    }
}
