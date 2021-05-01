package com.kafka.stream.serde;

import com.kafka.stream.proto.RatedMovieEvent.RatedMovie;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class RatedMovieSerializer implements Serializer<RatedMovie> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, RatedMovie movie) {
        return movie.toByteArray();
    }

    @Override
    public void close() {
    }
}
