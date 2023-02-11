package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RatingRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RatingRepository.class);


    private DataSource dataSource;

    public RatingRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createOrUpdate(Rating rating) {
        String query = "SELECT movieId, userId, rating FROM ratings WHERE movieId = " + rating.getMovieId() + " AND userID = " + rating.getUserId();
        String query2 = "update ratings SET rating = ? WHERE movieId = ? AND userId = ?";
        String query3 = "insert into ratings(movieId, userId, rating) values (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)
        ) {
            if (rs.next()) {
                int oldRating = rs.getInt("rating");
                PreparedStatement preparedStatement = connection.prepareStatement(query2);
                preparedStatement.setInt(1, rating.getRating());
                preparedStatement.setInt(2, rating.getMovieId());
                preparedStatement.setInt(3, rating.getUserId());
                preparedStatement.executeUpdate();
                String opis = String.format("Korisnik sa ID: %d promenio je ocenu filma sa ID: %d, sa ocene %d na ocenu %d!", rating.getUserId(), rating.getMovieId(), oldRating, rating.getRating());
                LOG.info(opis);
            } else {
                PreparedStatement preparedStatement = connection.prepareStatement(query3);
                preparedStatement.setInt(1, rating.getMovieId());
                preparedStatement.setInt(2, rating.getUserId());
                preparedStatement.setInt(3, rating.getRating());
                preparedStatement.executeUpdate();
                String opis = String.format("Korisnik sa ID: %d ocenio je film sa ID: %d, ocenom: %d", rating.getUserId(), rating.getMovieId(), rating.getRating());
                LOG.info(opis);
            }
        } catch (SQLException e) {
            String opis = String.format("Ocenjivanje filma sa ID: %d od strane korisnika sa ID: %d nije uspelo!", rating.getMovieId(), rating.getUserId());
            LOG.warn(opis);
        }
    }

    public List<Rating> getAll(String movieId) {
        List<Rating> ratingList = new ArrayList<>();
        String query = "SELECT movieId, userId, rating FROM ratings WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                ratingList.add(new Rating(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            String opis = String.format("Dohvatanje rejtinga za film sa ID: %d nije uspelo!", Integer.parseInt(movieId));
            LOG.warn(opis);
        }
        return ratingList;
    }
}
