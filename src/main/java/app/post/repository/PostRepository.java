package app.post.repository;

import app.community.model.Community;
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
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("""
            SELECT p FROM Post p JOIN FETCH p.author JOIN FETCH p.community
            WHERE p.id = :id
            """)
    Optional<Post> findByIdWithAuthorAndCommunity(UUID id);

    @Query("""
            SELECT p FROM Post p JOIN FETCH p.author
            WHERE p.community = :community
            """)
    Page<Post> findByCommunityWithAuthor(Community community, Pageable pageable);

    @Query("""
            SELECT p FROM Post p JOIN FETCH p.community
            WHERE p.author = :author
            """)
    Page<Post> findByAuthorWithCommunity(User author, Pageable pageable);
}
