package app.comment.repository;

import app.comment.model.Comment;
import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Query("""
            WITH thread_chain AS (
                SELECT c.id AS id, c.parentComment.id AS parentId, 0 AS depth FROM Comment c
                WHERE c.id = :commentId
            
                UNION ALL
            
                SELECT c.id AS id, c.parentComment.id AS parentId, tc.depth + 1 AS depth FROM thread_chain tc
                JOIN Comment c ON c.id = tc.parentId
            )
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.id IN (SELECT tc.id FROM thread_chain tc)
            ORDER BY c.createdOn
            """)
    List<Comment> findCommentThread(UUID commentId);

    @Query("""
            UPDATE Comment c SET c.replyCount = c.replyCount + 1
            WHERE c.id = :commentId
            """)
    @Modifying
    @Transactional
    void incrementReplyCount(UUID commentId);
}
