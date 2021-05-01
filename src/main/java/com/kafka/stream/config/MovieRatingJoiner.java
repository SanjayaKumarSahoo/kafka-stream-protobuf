package com.kafka.stream.config;


import com.kafka.stream.proto.MovieEvent;
import com.kafka.stream.proto.RatedMovieEvent;
import com.kafka.stream.proto.RatingEvent;
import org.apache.kafka.streams.kstream.ValueJoiner;


public class MovieRatingJoiner implements ValueJoiner<RatingEvent.Rating, MovieEvent.Movie, RatedMovieEvent.RatedMovie> {
    public RatedMovieEvent.RatedMovie apply(RatingEvent.Rating rating, MovieEvent.Movie movie) {
        return RatedMovieEvent.RatedMovie.newBuilder()
                .setId(movie.getId())
                .setTitle(movie.getTitle())
                .setReleaseYear(movie.getReleaseYear())
                .setRating(rating.getRating())
                .build();
    }
}