package com.kafka.stream.serde;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.kafka.stream.proto.RatedMovieEvent.RatedMovie;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class RatedMovieDeSerializer implements Deserializer<RatedMovie> {

    private final Parser<RatedMovie> parser;

    public RatedMovieDeSerializer() {
        parser = RatedMovie.parser();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public RatedMovie deserialize(String s, byte[] bytes) {
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
