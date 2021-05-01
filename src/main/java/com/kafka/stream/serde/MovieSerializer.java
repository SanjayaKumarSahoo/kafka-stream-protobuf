package com.kafka.stream.serde;

import com.kafka.stream.proto.MovieEvent;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class MovieSerializer implements Serializer<com.kafka.stream.proto.MovieEvent.Movie> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, MovieEvent.Movie movie) {
        return movie.toByteArray();
    }

    @Override
    public void close() {
    }
}
