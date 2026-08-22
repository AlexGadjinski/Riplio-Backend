package app.comment.repository;

import app.comment.model.Comment;
import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.post = :post AND c.parentComment IS NULL
            """)
    Page<Comment> findTopLevelByPostWithAuthor(Post post, Pageable pageable);

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.parentComment = :parentComment
            """)
    Page<Comment> findByParentCommentWithAuthor(Comment parentComment, Pageable pageable);

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.post p JOIN FETCH p.community
            WHERE c.author = :author AND c.status = 'ACTIVE'
            """)
    Page<Comment> findByAuthorWithPostAndCommunity(User author, Pageable pageable);

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.id = :id
            """)
    Optional<Comment> findByIdWithAuthor(UUID id);
}
