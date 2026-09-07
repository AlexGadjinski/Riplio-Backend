package app.post.model;

import app.common.model.Rippleable;
import app.community.model.Community;
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
@Table(name = "posts")
public class Post implements Rippleable {

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
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    @Override
    public void incrementRippleScore() {
        rippleScore++;
    }

    @Override
    public void decrementRippleScore() {
        rippleScore--;
    }
}
