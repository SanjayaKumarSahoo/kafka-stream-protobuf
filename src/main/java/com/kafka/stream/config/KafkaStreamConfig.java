package com.kafka.stream.config;

import com.kafka.stream.proto.MovieEvent;
import com.kafka.stream.proto.RatingEvent;
import com.kafka.stream.serde.MovieDeSerializer;
import com.kafka.stream.serde.RatingDeSerializer;
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
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        return properties;
    }

    private Topology buildTopology(Environment envProps, MovieRatingJoiner movieRatingJoiner) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String movieTopic = envProps.getProperty("kafka.movie.topic.name");
        final String rekeyedMovieTopic = envProps.getProperty("kafka.rekeyed.movie.topic.name");
        final String ratingTopic = envProps.getProperty("kafka.rating.topic.name");
        final String ratedMoviesTopic = envProps.getProperty("kafka.rated.movies.topic.name");

        KStream<Long, byte[]> movieStream =
                builder.stream(movieTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()))
                        .map((keyByte, movieByte) -> {
                            MovieDeSerializer movieDeSerializer = new MovieDeSerializer();
                            MovieEvent.Movie movie = movieDeSerializer.deserialize("", movieByte);
                            return new KeyValue<>(movie.getId(), movieByte);
                        });

        movieStream.to(rekeyedMovieTopic, Produced.with(Serdes.Long(), Serdes.ByteArray()));

        KTable<Long, byte[]> movies = builder.table(rekeyedMovieTopic);

        KStream<Long, byte[]> ratings =
                builder.stream(ratingTopic, Consumed.with(Serdes.ByteArray(), Serdes.ByteArray()))
                        .map((keyByte, ratingBte) -> {
                            RatingDeSerializer ratingDeSerializer = new RatingDeSerializer();
                            RatingEvent.Rating rating = ratingDeSerializer.deserialize("", ratingBte);
                            return new KeyValue<>(rating.getId(), ratingBte);
                        });

        KStream<Long, byte[]> ratedMovie = ratings.join(movies, movieRatingJoiner);

        ratedMovie.to(ratedMoviesTopic, Produced.with(Serdes.Long(), Serdes.ByteArray()));
        return builder.build();
    }
}