package app.community.repository;

import app.community.model.Community;
import app.community.model.CommunityBan;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBanRepository extends JpaRepository<CommunityBan, UUID> {

    boolean existsByBannedMemberAndCommunity(User bannedMember, Community community);

    Optional<CommunityBan> findByBannedMemberAndCommunity(User bannedMember, Community community);
}
