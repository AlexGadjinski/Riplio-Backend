package app.post.model;

import app.comment.model.Comment;
import app.community.model.Community;
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
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Basic
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private PostMediaType mediaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User author;

    @Column(nullable = false)
    private int commentCount;

    @Column(nullable = false)
    private int rippleScore;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> comments;

    public void incrementCommentCount() {
        commentCount++;
    }

    public void incrementRippleScore() {
        rippleScore++;
    }

    public void decrementRippleScore() {
        rippleScore--;
    }
}
