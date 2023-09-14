package fr.metamorpion.api.model;

import fr.metamorpion.api.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;
import java.util.UUID;

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
        int i = r.nextInt(3);
        int j = r.nextInt(3);
        int bigI = r.nextInt(3);
        int bigJ = r.nextInt(3);
        String subgridToPlayId = game.getSubgridToPlayId();

        if (subgridToPlayId == null) {
            Subgrid[][] subgrids = game.getGrid().getSubgrids();
            while (!subgrids[bigI][bigJ].isPlayable()) {
                bigI = r.nextInt(3);
                bigJ = r.nextInt(3);
            }
            subgridToPlayId = subgrids[bigI][bigJ].getUuid();
        }

        Subgrid subgridToPlay = getSubGrid(game, subgridToPlayId);
        assert subgridToPlay != null;
        while (subgridToPlay.getCells()[i][j] != CellStatus.EMPTY) {
            i = r.nextInt(3);
            j = r.nextInt(3);
        }
        return new int[]{bigI*3 + i, bigJ*3 + j};
    }

    private Subgrid getSubGrid(Game game, String subGridUuid) {
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
