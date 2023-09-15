package fr.metamorpion.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDTO {

    private int i;
    private int j;
    private String playerUUID;
    private boolean local = true;

}
