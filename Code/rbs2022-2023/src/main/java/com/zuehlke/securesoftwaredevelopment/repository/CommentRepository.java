package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CommentRepository.class);

    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CommentRepository.class);

    private DataSource dataSource;

    public CommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(Comment comment) {
        if(comment != null && !comment.getComment().equalsIgnoreCase("")){
            if(!(comment.getMovieId() <= 0 || comment.getUserId() == null)){
                String query = "insert into comments(movieId, userId, comment) values (?, ?, ?)";
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query);
                ) {
                    statement.setInt(1, comment.getMovieId());
                    statement.setInt(2, comment.getUserId());
                    statement.setString(3, comment.getComment());
                    statement.execute();
                    String opis = "dodao komentar: " + comment.getComment() + ", za film sa ID:" + comment.getMovieId();
                    auditLogger.auditDetailedDescription(opis);
                } catch (SQLException e) {
                    LOG.error("Greska pri dodavanju komentara!");
                }
            }else{
                LOG.error("Greska u ulaznim podacima pri dodavanju komentara!");
            }
        }else{
            LOG.error("Novi komentar za film ne moze biti prazan!");
        }
    }

    public List<Comment> getAll(String movieId) {
        List<Comment> commentList = new ArrayList<>();
        String query = "SELECT movieId, userId, comment FROM comments WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                commentList.add(new Comment(rs.getInt(1), rs.getInt(2), rs.getString(3)));
            }
            LOG.info("Prikazani komentari za film sa ID: " + movieId);
        } catch (SQLException e) {
            LOG.warn("Dohvatanje svih komentara za film sa ID: {} neuspesno!", movieId);
        }
        return commentList;
    }
}
