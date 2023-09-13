package fr.metamorpion.api.model;

import fr.metamorpion.api.constants.GameConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class Game {
    private static final String ROOM_FORMAT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    private final String roomCode;
    private final Grid grid;

    private Player player1, player2;
    private Player winner;
    private String currentPlayerId = "";
    private String subgridToPlayId;
    private CellStatus currentSymbol = CellStatus.X;
    private boolean started = false;
    private boolean finished = false;
    private GameType gameType;

    public Game(GameType gameType) {
        this.roomCode = Game.generateRoomCode(GameConstants.ROOMCODE_SIZE);
        this.grid = new Grid(GameConstants.GRID_SIZE);
        this.gameType = gameType;
    }

    private static String generateRoomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        final Random secureRnd = new Random();
        for (int i = 0; i < length; i++)
            sb.append(ROOM_FORMAT.charAt(secureRnd.nextInt(ROOM_FORMAT.length())));
        return sb.toString();
    }
}
