package com.shuttlebooking.court;

import com.shuttlebooking.venue.Venue;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "courts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "court_number", nullable = false)
    private int courtNumber;

    private String name;

    @Column(name = "price_per_hour_sgd", nullable = false)
    private BigDecimal pricePerHourSgd;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
