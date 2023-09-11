package fr.metamorpion.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_action")
@Getter
@Setter
@NoArgsConstructor
public class LogAction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "player_uuid")
    private String playerUUID;

    @Column(name = "game_uuid")
    private String gameUUID;

    @Column(name = "x_coordinate")
    private Long x;

    @Column(name = "y_coordinate")
    private Long y;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    void prePersist() {
        this.timestamp = LocalDateTime.now();
    }

}
