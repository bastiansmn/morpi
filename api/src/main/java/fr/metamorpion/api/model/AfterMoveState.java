package fr.metamorpion.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AfterMoveState {
    private final String type = "AFTER_MOVE";
    private final String roomCode;
    private final String playerUUID;
    private final String nextPlayerUUID;
    private final CellStatus nextSymbol;
    private final String subgridToPlayId;
    private final String completedSubgridId;
    private final int i;
    private final int j;
    private final boolean isGameFinished;
    private final Player winner;
}
