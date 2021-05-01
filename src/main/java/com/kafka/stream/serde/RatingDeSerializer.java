package com.kafka.stream.serde;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.kafka.stream.proto.RatingEvent.Rating;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class RatingDeSerializer implements Deserializer<Rating> {

    private final Parser<Rating> parser;

    public RatingDeSerializer() {
        parser = Rating.parser();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Rating deserialize(String s, byte[] bytes) {
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
