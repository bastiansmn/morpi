package fr.metamorpion.api.model;

import fr.metamorpion.api.constants.GameConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Grid {

    private final String uuid;
    private final Subgrid[][] subgrids;

    public Grid(int size) {
        this.uuid = UUID.randomUUID().toString();
        this.subgrids = new Subgrid[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.subgrids[i][j] = new Subgrid(GameConstants.SUBGRID_SIZE);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Grid g) {
            return this.uuid.equals(g.uuid);
        }
        return false;
    }
}
