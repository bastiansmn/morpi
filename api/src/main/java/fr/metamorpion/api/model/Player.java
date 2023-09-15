package fr.metamorpion.api.model;

import fr.metamorpion.api.service.GameService;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;
import java.util.UUID;

import static fr.metamorpion.api.constants.GameConstants.GRID_SIZE;

@Getter
@Setter
@AllArgsConstructor
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

    private int[] bestNextMove(Game game, boolean isIATurn){
        if(game.isFinished()) return null;

        int i = 0, j = 0;
        if(isIATurn) {

        } else {

        }
        return new int[]{i, j};
    }

    public int[] iaMakeAMove(Game game) {
        Random r = new Random();
        int i = r.nextInt(GRID_SIZE);
        int j = r.nextInt(GRID_SIZE);
        String subgridToPlayId = game.getSubgridToPlayId();
        Subgrid subgridToPlay;
        Integer bigI = null;
        Integer bigJ = null;

        // If the subgrid to play is null, we choose a random subgrid
        if (subgridToPlayId == null) {
            bigI = r.nextInt(GRID_SIZE);
            bigJ = r.nextInt(GRID_SIZE);
            Subgrid[][] subgrids = game.getGrid().getSubgrids();
            while (!subgrids[bigI][bigJ].isPlayable()) {
                bigI = r.nextInt(GRID_SIZE);
                bigJ = r.nextInt(GRID_SIZE);
            }
        } else {
            // We get the coordinates of the subgrid to play
            for (int k = 0; k < GRID_SIZE; k++) {
                for (int l = 0; l < GRID_SIZE; l++) {
                    if (game.getGrid().getSubgrids()[k][l].getUuid().equals(subgridToPlayId)) {
                        bigI = k;
                        bigJ = l;
                        break;
                    }
                }
            }
        }

        assert bigI != null && bigJ != null;
        subgridToPlay = game.getGrid().getSubgrids()[bigI][bigJ];

        assert subgridToPlay != null;
        while (subgridToPlay.getCells()[i][j] != CellStatus.EMPTY) {
            i = r.nextInt(GRID_SIZE);
            j = r.nextInt(GRID_SIZE);
        }
        return new int[]{bigI*3 + i, bigJ*3 + j};
    }

    private Subgrid getSubGrid(Game game, @Nullable String subGridUuid) {
        Subgrid[][] subGrids = game.getGrid().getSubgrids();
        for(Subgrid[] subgrid: subGrids) {
            for (Subgrid sub: subgrid) {
                if (sub.getUuid().equals(subGridUuid)) {
                    return sub;
                }
            }
        }
        return null;
    }
}
