package app.ripple.repository;

import app.post.model.Post;
import app.ripple.model.PostRipple;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRippleRepository extends JpaRepository<PostRipple, UUID> {

    Optional<PostRipple> findByPostAndAuthor(Post post, User author);

    List<PostRipple> findByPostInAndAuthor(List<Post> posts, User author);
}
