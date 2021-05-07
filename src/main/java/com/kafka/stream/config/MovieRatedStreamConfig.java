package com.kafka.stream.config;

import com.kafka.stream.serde.MovieDeSerializer;
import com.kafka.stream.serde.RatingDeSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
@Profile("movie-rated")
public class MovieRatedStreamConfig {

    @Bean
    public MovieDeSerializer movieDeSerializer() {
        return new MovieDeSerializer();
    }

    @Bean
    public RatingDeSerializer ratingDeSerializer() {
        return new RatingDeSerializer();
    }

    @Bean
    public MovieRatingJoiner movieRatingJoiner(MovieDeSerializer movieDeSerializer, RatingDeSerializer ratingDeSerializer) {
        return new MovieRatingJoiner(movieDeSerializer, ratingDeSerializer);
    }

    @Bean(destroyMethod = "close")
    @Qualifier("movingRatedKafkaStream")
    public KafkaStreams movingRatedKafkaStream(Environment envProps, MovieRatingJoiner movieRatingJoiner) {
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
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");
        return properties;
    }

    private Topology buildTopology(Environment envProps, MovieRatingJoiner movieRatingJoiner) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String movieTopic = envProps.getProperty("kafka.movie.topic.name");
        final String rekeyedMovieTopic = envProps.getProperty("kafka.rekeyed.movie.topic.name");
        final String ratingTopic = envProps.getProperty("kafka.rating.topic.name");
        final String ratedMoviesTopic = envProps.getProperty("kafka.rated.movies.topic.name");

        KStream<byte[], byte[]> movieStream =
                builder.stream(movieTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()))
                        .map(KeyValue::new);

        movieStream.to(rekeyedMovieTopic, Produced.with(Serdes.ByteArray(), Serdes.ByteArray()));

        KTable<byte[], byte[]> movies = builder.table(rekeyedMovieTopic);

        KStream<byte[], byte[]> ratings =
                builder.stream(ratingTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()))
                        .map(KeyValue::new);

        KStream<byte[], byte[]> ratedMovie = ratings.join(movies, movieRatingJoiner);

        ratedMovie.to(ratedMoviesTopic, Produced.with(Serdes.ByteArray(), Serdes.ByteArray()));
        return builder.build();
    }
}