package app.community.repository;

import app.community.model.CommunityMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, UUID> {

}
