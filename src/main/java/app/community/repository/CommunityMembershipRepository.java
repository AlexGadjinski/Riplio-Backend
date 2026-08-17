package app.community.repository;

import app.community.model.Community;
import app.community.model.CommunityMembership;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, UUID> {

    boolean existsByMemberAndCommunity(User member, Community community);
}
