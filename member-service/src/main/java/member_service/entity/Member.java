package member_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType subscriptionType;

    @Column(nullable = false)
    private boolean suspended = false;

    /**
     * Déduit automatiquement du type d'abonnement.
     */
    @Column(nullable = false)
    private Integer maxConcurrentBookings;

    @PrePersist
    @PreUpdate
    private void syncMaxBookings() {
        if (subscriptionType != null) {
            this.maxConcurrentBookings = subscriptionType.getMaxConcurrentBookings();
        }
    }
}
