package app.ripple.repository;

import app.comment.model.Comment;
import app.ripple.model.CommentRipple;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRippleRepository extends JpaRepository<CommentRipple, UUID> {

    Optional<CommentRipple> findByCommentAndAuthor(Comment comment, User author);
}
