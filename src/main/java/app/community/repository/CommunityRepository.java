package app.community.repository;

import app.community.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {

    boolean existsByName(String name);

    @Query("SELECT c FROM Community c JOIN FETCH c.creator WHERE c.id = :id")
    Optional<Community> findByIdWithCreator(UUID id);
}
