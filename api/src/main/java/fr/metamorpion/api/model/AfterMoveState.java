package fr.metamorpion.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AfterMoveState {
    private final String roomCode;
    private final String playerUUID;
    private final int i;
    private final int j;
    private final boolean isGameFinished;
}
