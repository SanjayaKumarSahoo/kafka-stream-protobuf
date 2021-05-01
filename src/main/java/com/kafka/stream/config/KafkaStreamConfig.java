package com.kafka.stream.config;

import com.kafka.stream.proto.MovieEvent;
import com.kafka.stream.proto.RatedMovieEvent;
import com.kafka.stream.proto.RatingEvent;
import com.kafka.stream.serde.CustomSerdes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
public class KafkaStreamConfig {


    @Bean
    public MovieRatingJoiner movieRatingJoiner() {
        return new MovieRatingJoiner();
    }

    @Bean(destroyMethod = "close")
    public KafkaStreams kafkaStreams(Environment envProps, MovieRatingJoiner movieRatingJoiner) {
        Topology topology = buildTopology(envProps, movieRatingJoiner);
        final KafkaStreams streams = new KafkaStreams(topology, properties(envProps));
        streams.start();
        return streams;
    }

    private Properties properties(Environment envProps) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("kafka.bootstrap-servers"));
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("kafka.application-id"));
        properties.put(StreamsConfig.STATE_DIR_CONFIG, envProps.getProperty("kafka.state.dir"));
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, envProps.getProperty("kafka.client.id"));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, envProps.getProperty("kafka.group.id"));
        return properties;
    }

    private Topology buildTopology(Environment envProps, MovieRatingJoiner movieRatingJoiner) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String movieTopic = envProps.getProperty("kafka.movie.topic.name");
        final String rekeyedMovieTopic = envProps.getProperty("kafka.rekeyed.movie.topic.name");
        final String ratingTopic = envProps.getProperty("kafka.rating.topic.name");
        final String ratedMoviesTopic = envProps.getProperty("kafka.rated.movies.topic.name");

        KStream<String, MovieEvent.Movie> movieStream =
                builder.stream(movieTopic, Consumed.with(Serdes.String(), CustomSerdes.movie()))
                        .map((key, movie) -> new KeyValue<>(String.valueOf(movie.getId()), movie));

        movieStream.to(rekeyedMovieTopic, Produced.with(Serdes.String(), CustomSerdes.movie()));

        KTable<String, MovieEvent.Movie> movies = builder.table(rekeyedMovieTopic);

        KStream<String, RatingEvent.Rating> ratings =
                builder.stream(ratingTopic, Consumed.with(Serdes.String(), CustomSerdes.rating()))
                        .map((key, rating) -> new KeyValue<>(String.valueOf(rating.getId()), rating));

        KStream<String, RatedMovieEvent.RatedMovie> ratedMovie = ratings.join(movies, movieRatingJoiner);

        ratedMovie.to(ratedMoviesTopic, Produced.with(Serdes.String(), CustomSerdes.ratedMovie()));
        return builder.build();
    }
}