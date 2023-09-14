package fr.metamorpion.api.model.api_e;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GridDTO {
    private final int row;
    private final int column;
    private final int childRow;
    private final int childColumn;
    private final String value;
}
