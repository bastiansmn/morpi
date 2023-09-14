package fr.metamorpion.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActionDTO {

    private int i;
    private int j;
    private String playerUUID;

}
