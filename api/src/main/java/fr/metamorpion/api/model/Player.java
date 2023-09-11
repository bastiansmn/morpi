package fr.metamorpion.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Player {

    private final String uuid;
    private final String username;

    public Player(String username) {
        this.uuid = UUID.randomUUID().toString();
        this.username = username;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player p) {
            return this.uuid.equals(p.uuid);
        }

        return false;
    }
}
