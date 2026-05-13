package bioCanteenApp.notifications.domain;

import bioCanteenApp.notifications.dto.NotificationType;
import bioCanteenApp.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "priority")
    private Integer priority;

    public Notification() {

    }

    public Notification(User user, String title, String message, NotificationType type, LocalDateTime createdAt, boolean read, String actionUrl, Integer priority) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
        this.actionUrl = actionUrl;
        this.priority = priority;
    }
}
