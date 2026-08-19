package app.community.repository;

import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.community.model.CommunityRole;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, UUID> {

    boolean existsByMemberAndCommunity(User member, Community community);

    Optional<CommunityMembership> findByMemberAndCommunity(User member, Community community);

    @Query("""
            SELECT cm FROM CommunityMembership cm JOIN FETCH cm.member
            WHERE cm.community = :community AND (:role IS NULL OR cm.role = :role)
            """)
    Page<CommunityMembership> findByCommunityAndRoleWithMember(Community community,
                                                               CommunityRole role,
                                                               Pageable pageable);
}
