package app.comment.model;

import app.common.model.Rippleable;
import app.post.model.Post;
import app.ripple.model.CommentRipple;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment implements Rippleable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Basic
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment parentComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    @Column(nullable = false)
    private int replyCount;

    @Column(nullable = false)
    private int rippleScore;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
    private List<CommentRipple> ripples;

    public boolean isActive() {
        return status == CommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return status == CommentStatus.DELETED;
    }

    public boolean isRemoved() {
        return status == CommentStatus.REMOVED;
    }

    public void incrementReplyCount() {
        replyCount++;
    }

    @Override
    public void incrementRippleScore() {
        rippleScore++;
    }

    @Override
    public void decrementRippleScore() {
        rippleScore--;
    }
}
