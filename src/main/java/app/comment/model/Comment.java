package app.comment.model;

import app.post.model.Post;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Basic
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    public boolean isActive() {
        return status == CommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return status == CommentStatus.DELETED;
    }

    public boolean isRemoved() {
        return status == CommentStatus.REMOVED;
    }
}
