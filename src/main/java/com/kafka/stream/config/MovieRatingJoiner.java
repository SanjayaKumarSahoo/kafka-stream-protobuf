package com.kafka.stream.config;


import com.kafka.stream.proto.MovieEvent;
import com.kafka.stream.proto.RatedMovieEvent;
import com.kafka.stream.proto.RatingEvent;
import com.kafka.stream.serde.MovieDeSerializer;
import com.kafka.stream.serde.RatingDeSerializer;
import org.apache.kafka.streams.kstream.ValueJoiner;


public class MovieRatingJoiner implements ValueJoiner<byte[], byte[], byte[]> {
    public byte[] apply(byte[] ratingByte, byte[] movieByte) {

        MovieDeSerializer movieDeSerializer = new MovieDeSerializer();
        MovieEvent.Movie movie = movieDeSerializer.deserialize("", movieByte);

        RatingDeSerializer ratingDeSerializer = new RatingDeSerializer();
        RatingEvent.Rating rating = ratingDeSerializer.deserialize("", ratingByte);

        RatedMovieEvent.RatedMovie ratedMovie = RatedMovieEvent.RatedMovie.newBuilder()
                .setId(movie.getId())
                .setTitle(movie.getTitle())
                .setReleaseYear(movie.getReleaseYear())
                .setRating(rating.getRating())
                .build();
        return ratedMovie.toByteArray();
    }
}