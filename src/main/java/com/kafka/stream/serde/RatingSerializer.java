package com.kafka.stream.serde;

import com.kafka.stream.proto.RatingEvent.Rating;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class RatingSerializer implements Serializer<Rating> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Rating rating) {
        return rating.toByteArray();
    }

    @Override
    public void close() {
    }
}
