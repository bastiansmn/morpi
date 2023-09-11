package fr.metamorpion.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Subgrid {

    private final String uuid;

    private final CellStatus[][] cells;

    private boolean playable = true;

    private CellStatus winner = CellStatus.EMPTY;

    public Subgrid(int size) {
        this.uuid = UUID.randomUUID().toString();
        this.cells = new CellStatus[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.cells[i][j] = CellStatus.EMPTY;
            }
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Subgrid g) {
            return this.uuid.equals(g.uuid);
        }
        return false;
    }
}
