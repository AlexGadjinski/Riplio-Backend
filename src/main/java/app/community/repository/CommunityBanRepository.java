package app.community.repository;

import app.community.model.Community;
import app.community.model.CommunityBan;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBanRepository extends JpaRepository<CommunityBan, UUID> {

    boolean existsByBannedMemberAndCommunity(User bannedMember, Community community);

    Optional<CommunityBan> findByBannedMemberAndCommunity(User bannedMember, Community community);

    @Query("""
            SELECT b FROM CommunityBan b
            JOIN FETCH b.bannedMember
            JOIN FETCH b.bannedBy
            WHERE b.community = :community
            """)
    Page<CommunityBan> findByCommunityWithBannedMember(Community community, Pageable pageable);

    void deleteByCommunity(Community community);
}
