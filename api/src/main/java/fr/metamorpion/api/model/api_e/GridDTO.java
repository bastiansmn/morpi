package fr.metamorpion.api.model.api_e;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GridDTO {
    private int row;
    private int column;
    private int childRow;
    private int childColumn;
    private String value;
}
