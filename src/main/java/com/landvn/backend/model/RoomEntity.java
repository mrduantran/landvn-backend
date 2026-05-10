package com.landvn.backend.model;

import com.landvn.backend.util.GameStateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    private String id;

    private String name;

    private String status;

    private String winnerId;

    @Convert(converter = GameStateConverter.class)
    @Column(name = "game_state", columnDefinition = "text")
    private GameState gameState;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_delete")
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
