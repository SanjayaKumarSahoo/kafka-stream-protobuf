package com.kafka.stream.serde;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.kafka.stream.proto.MovieEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class MovieDeSerializer implements Deserializer<MovieEvent.Movie> {

    private final Parser<MovieEvent.Movie> parser;

    public MovieDeSerializer() {
        parser = MovieEvent.Movie.parser();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public MovieEvent.Movie deserialize(String s, byte[] bytes) {
        try {
            return parser.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException("Error de-serializing from Protocol buffer message", e);
        }
    }

    @Override
    public void close() {
    }
}
