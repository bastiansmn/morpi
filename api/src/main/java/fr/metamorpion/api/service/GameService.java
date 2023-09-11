package fr.metamorpion.api.service;

import fr.metamorpion.api.exception.FunctionalException;
import fr.metamorpion.api.exception.FunctionalRule;
import fr.metamorpion.api.model.CellStatus;
import fr.metamorpion.api.model.Game;
import fr.metamorpion.api.model.GameType;
import fr.metamorpion.api.model.Player;
import fr.metamorpion.api.model.Subgrid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameService {

    private final LogActionService logActionService;

    // Map associating player's UUID to the player object
    private final Map<String, Player> players = new HashMap<>();

    // Map associating roomCode to the game object
    private final Map<String, Game> games = new HashMap<>();

    public Collection<Game> getPendingGames() {
        return games.values().stream().filter(g -> !g.isStarted()).toList();
    }

    /***
     * Create a new game
     *
     * @param gameType Choose the type of game you want to start
     * @return the game just created
     */
    public Game createGame(GameType gameType, String playerUUID) throws FunctionalException {
        Player player = players.getOrDefault(playerUUID, null);
        if (player == null) {
            player = registerPlayer("guest");
        }

        switch (gameType) {
            default -> {
                var game = new Game();
                game.setPlayer1(player);
                games.put(game.getRoomCode(), game);
                return game;
            }
        }
    }

    /**
     * Make a player join the specified room
     *
     * @param roomCode game room code
     * @param playerUUID the player uuid to join (create if not exists)
     * @return the game of the roomCode
     * @throws FunctionalException if the game isn't found.
     */
    public Game joinGame(String roomCode, String playerUUID) throws FunctionalException {
        Game game = games.getOrDefault(roomCode, null);
        if (Objects.isNull(game)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0001
            );
        }

        Player player = players.getOrDefault(playerUUID, null);
        if (player == null) {
            player = registerPlayer("guest");
        }
        addPlayer(game, player);
        return game;
    }

    public void quitGame(String roomCode, String playerUUID) throws FunctionalException {
        Game game = games.getOrDefault(roomCode, null);
        if (Objects.isNull(game)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0001
            );
        }

        Player player = players.getOrDefault(playerUUID, null);
        if (player == null) {
            throw new FunctionalException(
                    FunctionalRule.PLAYER_0001
            );
        }

        if (!game.getPlayer1().equals(player) && !game.getPlayer1().equals(player)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0004,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (game.getPlayer1().equals(player)) {
            game.setPlayer1(null);
        } else {
            game.setPlayer2(null);
        }
    }

    public void deleteGame(String roomCode) throws FunctionalException {
        Game game = games.getOrDefault(roomCode, null);
        if (Objects.isNull(game)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0001
            );
        }

        games.remove(roomCode);
        logActionService.deleteAllByGameUUID(roomCode);
    }

    public Game findByRoomCode(String roomCode) throws FunctionalException {
        Game game = games.getOrDefault(roomCode, null);
        if (game == null) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0001
            );
        }
        return game;
    }

    private void addPlayer(Game game, Player p) throws FunctionalException {
        if (game.getPlayer1() == null) {
            game.setPlayer1(p);
            return;
        }

        if (game.getPlayer1().equals(p)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0003,
                    HttpStatus.BAD_REQUEST
            );
        }

        if (game.getPlayer2() == null) {
            game.setPlayer2(p);
            return;
        }

        if (game.getPlayer2().equals(p)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0003,
                    HttpStatus.BAD_REQUEST
            );
        }

        throw new FunctionalException(
                FunctionalRule.GAME_0002,
                HttpStatus.BAD_REQUEST
        );
    }

    public Player registerPlayer(String username) {
        var player = new Player(username);
        players.put(player.getUuid(), player);
        return player;
    }

    /**
     * Check if a move is possible in the grid of the game
     * @param game current game
     * @param i position i of the move
     * @param j position j of the move
     * @param subGridUuid the subgrid's uuid of our move
     * @return true if it's possible
     */
    public boolean isAMovePossible(Game game, int i, int j, String subGridUuid) {
        return cellIsEmpty(game, i, j, subGridUuid) &&
                subGridPlayable(game, subGridUuid) &&
                inTheSubGrid(game, subGridUuid);
        // TODO verifier que c'est le bon joueur qui essaie de jouer
    }

    /**
     * Look if a cell is empty
     * @param game current game
     * @param i position i of the move
     * @param j position j of the move
     * @param subGridUuid the subgrid's uuid of a move
     * @return true if the cell is empty
     */
    private boolean cellIsEmpty(Game game, int i, int j, String subGridUuid) {
        Subgrid subgrid = getSubGrid(game, subGridUuid);
        assert subgrid != null;
        return subgrid.getCells()[i][j].equals(CellStatus.EMPTY);
    }

    /**
     * Look if a subGrid is playable
     * @param game the current game
     * @param subGridUuid the subgrid's uuid we want to check
     * @return true if the subGrid is playable
     */
    private boolean subGridPlayable(Game game, String subGridUuid) {
        Subgrid subgrid = getSubGrid(game, subGridUuid);
        assert subgrid != null;
        return subgrid.isPlayable();
    }

    /**
     * Check if our subGrid is the actual subGrid we need to play in
     * (check si notre subgrid est celle où on doit jouer)
     * @param game the current game
     * @param subGridUuid the subgrid's uuid we want to make a move
     * @return true
     */
    private boolean inTheSubGrid(Game game, String subGridUuid) {
        return game.getSubgridToPlayId().equals(subGridUuid);
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

    /**
     *
     * @param game
     * @param i
     * @param j
     * @param subGribUuid
     * @return si le jeu est fini ou non
     */
    public boolean playAMove(Game game, int i, int j, String subGribUuid) {
        Subgrid subgrid = getSubGrid(game, subGribUuid);
        assert subgrid != null;

        subgrid.getCells()[i][j] = game.getCurrentSymbol();

        if (game.getCurrentSymbol().equals(CellStatus.X)) {
            game.setCurrentSymbol(CellStatus.O);
        } else if (game.getCurrentSymbol().equals(CellStatus.O)){
            game.setCurrentSymbol(CellStatus.X);
        }

        calculateTheSubGridToPlay(game, i, j);

        if (game.getCurrentPlayerId().equals(game.getPlayer1().getUuid())) {
            game.setCurrentPlayerId(game.getPlayer2().getUuid());
        } else if (game.getCurrentPlayerId().equals(game.getPlayer2().getUuid())){
            game.setCurrentPlayerId(game.getPlayer1().getUuid());
        }

        // TODO i * 3*i de la subgrid, pareil avec j
        logActionService.saveLogAction(
                "",
                game.getRoomCode(),
                (long) i,
                (long) j
        );
        if (isSubgridFinished(subgrid)) return isGameFinished(game);
        return false;
    }

    private void calculateTheSubGridToPlay(Game game, int i, int j) {
        game.setSubgridToPlayId(game.getGrid().getSubgrids()[i][j].getUuid());
    }

    private boolean isSubgridFinished(Subgrid subgrid) {
        CellStatus[][] cellStatus = subgrid.getCells();
        for(int i = 0; i < 3; i++) {
            if (isTheSameCells(i, true, cellStatus)) {
                subgrid.setWinner(subgrid.getCells()[0][i]);
                subgrid.setPlayable(false);
                return true;
            } else if (isTheSameCells(i, false, cellStatus)) {
                subgrid.setWinner(subgrid.getCells()[i][0]);
                subgrid.setPlayable(false);
                return true;
            }
        }
        if (isTheSameCellsForDiagonale(cellStatus)){
            subgrid.setWinner(subgrid.getCells()[1][1]);
            subgrid.setPlayable(false);
            return true;
        } else {
            return false;
        }
    }

    private boolean isTheSameCells(int y, boolean yIsJ, CellStatus[][] cells) {
        if (yIsJ) {
            if (cells[0][y] == CellStatus.EMPTY) return false;
            return ((cells[0][y] == cells[1][y]) && (cells[1][y] == cells[2][y]));
        } else {
            if (cells[y][0] == CellStatus.EMPTY) return false;
            return ((cells[y][0] == cells[y][1]) && (cells[y][1] == cells[y][2]));
        }
    }

    private boolean isTheSameCellsForDiagonale(CellStatus[][] cells) {
        boolean diag1 = (cells[0][0] == cells[1][1])
                && (cells[1][1] == cells[2][2])
                && cells[0][0] != CellStatus.EMPTY;
        boolean diag2 = (cells[2][0] == cells[1][1])
                && (cells[1][1] == cells[0][2])
                && cells[2][0] != CellStatus.EMPTY;
        return diag1 || diag2;

    }

    private boolean isGameFinished(Game game) {
        Subgrid[][] subgrids = game.getGrid().getSubgrids();
        CellStatus[][] gridToCells = new CellStatus[subgrids.length][subgrids[0].length];

        for(int i = 0; i < subgrids.length; i++) {
            for(int j = 0; j < subgrids[0].length; j++) {
                gridToCells[i][j] = subgrids[i][j].getWinner();
            }
        }

        for(int i = 0; i < 3; i++) {
            if (isTheSameCells(i, true, gridToCells)) {
                setTheWinner(gridToCells[0][i], game);
                return true;
            } else if (isTheSameCells(i, false, gridToCells)) {
                setTheWinner(gridToCells[i][0], game);
                return true;
            }
        }
        if (isTheSameCellsForDiagonale(gridToCells)){
            setTheWinner(gridToCells[1][1], game);
            return true;
        } else {
            return false;
        }
    }

    private void setTheWinner(CellStatus cellStatus, Game game) {
        if (cellStatus == CellStatus.X) {
            game.setWinner(game.getPlayer1());
        } else if(cellStatus == CellStatus.O) {
            game.setWinner(game.getPlayer2());
        }
    }
}
