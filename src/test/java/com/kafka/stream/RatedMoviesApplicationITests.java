package com.kafka.stream;

import com.kafka.stream.proto.MovieEvent;
import com.kafka.stream.proto.RatedMovieEvent;
import com.kafka.stream.proto.RatingEvent.Rating;
import com.kafka.stream.serde.MovieSerializer;
import com.kafka.stream.serde.RatedMovieDeSerializer;
import com.kafka.stream.serde.RatingSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@SpringBootTest
class RatedMoviesApplicationITests {


    @Test
    public void testGetRatedMovies() {

        // publish movies data
        MovieEvent.Movie dieHard = MovieEvent.Movie.newBuilder().setId(294).setTitle("Die Hard").setReleaseYear(1988).build();
        MovieEvent.Movie treeOfLife = MovieEvent.Movie.newBuilder().setId(354).setTitle("Tree of Life").setReleaseYear(2011).build();
        MovieEvent.Movie aWalkInTheClouds = MovieEvent.Movie.newBuilder().setId(782).setTitle("A Walk in the Clouds").setReleaseYear(1995).build();
        MovieEvent.Movie theBigLebowski = MovieEvent.Movie.newBuilder().setId(128).setTitle("The Big Lebowski").setReleaseYear(1998).build();
        MovieEvent.Movie superMarioBros = MovieEvent.Movie.newBuilder().setId(780).setTitle("Super Mario Bros").setReleaseYear(1993).build();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MovieSerializer.class);

        KafkaProducer<Long, MovieEvent.Movie> movieKafkaProducer = new KafkaProducer<>(props);
        movieKafkaProducer.send(new ProducerRecord<>("movies", dieHard.getId(), dieHard));
        movieKafkaProducer.send(new ProducerRecord<>("movies", treeOfLife.getId(), treeOfLife));
        movieKafkaProducer.send(new ProducerRecord<>("movies", aWalkInTheClouds.getId(), aWalkInTheClouds));
        movieKafkaProducer.send(new ProducerRecord<>("movies", theBigLebowski.getId(), theBigLebowski));
        movieKafkaProducer.send(new ProducerRecord<>("movies", superMarioBros.getId(), superMarioBros));

        // publish rating data
        Rating dieHardRating1 = Rating.newBuilder().setId(294).setRating(8.2).build();
        Rating dieHardRating2 = Rating.newBuilder().setId(294).setRating(8.5).build();
        Rating treeOfLifeRating1 = Rating.newBuilder().setId(354).setRating(9.9).build();
        Rating treeOfLifeRating2 = Rating.newBuilder().setId(354).setRating(9.7).build();
        Rating aWalkInTheCloudsRating1 = Rating.newBuilder().setId(782).setRating(7.8).build();
        Rating aWalkInTheCloudsRating2 = Rating.newBuilder().setId(782).setRating(7.7).build();
        Rating theBigLebowskiRating1 = Rating.newBuilder().setId(128).setRating(8.7).build();
        Rating theBigLebowskiRating2 = Rating.newBuilder().setId(128).setRating(8.4).build();
        Rating superMarioBrosRating = Rating.newBuilder().setId(780).setRating(2.1).build();

        Properties ratingProps = new Properties();
        ratingProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        ratingProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        ratingProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RatingSerializer.class);

        KafkaProducer<Long, Rating> ratingKafkaProducer = new KafkaProducer<>(ratingProps);
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", dieHardRating1.getId(), dieHardRating1));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", dieHardRating2.getId(), dieHardRating2));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", treeOfLifeRating1.getId(), treeOfLifeRating1));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", treeOfLifeRating2.getId(), treeOfLifeRating2));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", aWalkInTheCloudsRating1.getId(), aWalkInTheCloudsRating1));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", aWalkInTheCloudsRating2.getId(), aWalkInTheCloudsRating2));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", theBigLebowskiRating1.getId(), theBigLebowskiRating1));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", theBigLebowskiRating2.getId(), theBigLebowskiRating2));
        ratingKafkaProducer.send(new ProducerRecord<>("ratings", superMarioBrosRating.getId(), superMarioBrosRating));

        // consume rated movie data and print
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "client-group-id");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, RatedMovieDeSerializer.class);

        KafkaConsumer<Long, RatedMovieEvent.RatedMovie> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Arrays.asList("rated-movies"));
        ConsumerRecords<Long, RatedMovieEvent.RatedMovie> consumerRecords = consumer.poll(Duration.ofSeconds(5));
        consumerRecords.records("rated-movies").forEach(record -> {
            RatedMovieEvent.RatedMovie ratedMovie = record.value();
            System.out.println("id : " + ratedMovie.getId()
                    + " title : " + ratedMovie.getTitle()
                    + " release year : " + ratedMovie.getReleaseYear()
                    + " rating : " + ratedMovie.getRating());
        });

    }
}
