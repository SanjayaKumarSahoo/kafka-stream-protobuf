package com.kafka.stream.serde;

import com.kafka.stream.proto.MovieEvent.Movie;
import com.kafka.stream.proto.RatedMovieEvent.RatedMovie;
import com.kafka.stream.proto.RatingEvent.Rating;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class CustomSerdes {

    public static final class MovieSerde extends Serdes.WrapperSerde<Movie> {
        public MovieSerde() {
            super(new MovieSerializer(), new MovieDeSerializer());
        }
    }

    public static final class RatingSerde extends Serdes.WrapperSerde<Rating> {
        public RatingSerde() {
            super(new RatingSerializer(), new RatingDeSerializer());
        }
    }

    public static final class RatedMovieSerde extends Serdes.WrapperSerde<RatedMovie> {
        public RatedMovieSerde() {
            super(new RatedMovieSerializer(), new RatedMovieDeSerializer());
        }
    }

    public static Serde<Movie> movie() {
        return new CustomSerdes.MovieSerde();
    }

    public static Serde<Rating> rating() {
        return new CustomSerdes.RatingSerde();
    }

    public static Serde<RatedMovie> ratedMovie() {
        return new CustomSerdes.RatedMovieSerde();
    }
}
