package app.comment.repository;

import app.comment.model.Comment;
import app.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.post = :post AND c.parentComment IS NULL
            """)
    Page<Comment> findTopLevelByPostWithAuthor(Post post, Pageable pageable);
}
