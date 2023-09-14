package fr.metamorpion.api.model.api_d;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReceiveResponse {
    private final String bigX;
    private final String bigY;
    private final String littleX;
    private final String littleY;
}
