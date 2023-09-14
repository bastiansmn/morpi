package fr.metamorpion.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fr.metamorpion.api.configuration.StompSessionHandler;
import fr.metamorpion.api.configuration.properties.ExternalAPI;
import fr.metamorpion.api.configuration.properties.ExternalAPIProperties;
import fr.metamorpion.api.constants.GameConstants;
import fr.metamorpion.api.exception.FunctionalException;
import fr.metamorpion.api.exception.FunctionalRule;
import fr.metamorpion.api.model.ActionDTO;
import fr.metamorpion.api.model.CellStatus;
import fr.metamorpion.api.model.Game;
import fr.metamorpion.api.model.GameType;
import fr.metamorpion.api.model.Player;
import fr.metamorpion.api.model.Subgrid;
import fr.metamorpion.api.model.api_d.QuitResponse;
import fr.metamorpion.api.model.api_d.StartResponse;
import fr.metamorpion.api.model.api_e.GameE;
import fr.metamorpion.api.model.api_e.GridDTO;
import fr.metamorpion.api.model.api_e.PlayerE;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.ast.tree.expression.Star;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class GameService {

    private final LogActionService logActionService;

    // Map associating player's UUID to the player object
    private final Map<String, Player> players = new HashMap<>();

    // Map associating roomCode to the game object
    private final Map<String, Game> games = new HashMap<>();

    private final RestTemplate http;

    private final ExternalAPIProperties externalAPIProperties;

    private final WebSocketStompClient stompClient;
    private final StompSessionHandler stompSessionHandler;

    public Collection<Game> getPendingGames() {
        return games.values().stream()
                .filter(g -> !g.isStarted())
                .filter(g -> g.getGameType().equals(GameType.PVP_ONLINE))
                .filter(g -> g.getPlayer1() == null || g.getPlayer2() == null)
                .toList();
    }

    /***
     * Create a new game
     *
     * @param gameType Choose the type of game you want to start
     * @return the game just created
     */
    public Game createGame(GameType gameType, String playerUUID, boolean isFirstToPlay) throws FunctionalException {
        if (gameType.equals(GameType.PVP_ONLINE))
            initGameExternalAPI(gameType, isFirstToPlay);

        Player player = players.getOrDefault(playerUUID, null);
        if (player == null) {
            player = registerPlayer("guest");
        }

        switch (gameType) {
            case PVP_LOCAL -> {
                var game = new Game(gameType);
                game.setPlayer1(player);
                game.setPlayer2(registerPlayer("guest"));
                if (isFirstToPlay) {
                    game.setCurrentPlayerId(player.getUuid());
                }
                games.put(game.getRoomCode(), game);
                return game;
            }
            case PVP_ONLINE -> {
                var game = new Game(gameType);
                game.setPlayer1(player);
                if (isFirstToPlay) {
                    game.setCurrentPlayerId(player.getUuid());
                }
                games.put(game.getRoomCode(), game);
                return game;
            }
            default -> {
                var game = new Game(gameType);
                game.setPlayer1(player);
                game.setPlayer2(registerPlayer("IA"));
                game.setCurrentPlayerId(player.getUuid());
                games.put(game.getRoomCode(), game);
                return game;
            }
        }

    }

    public void initGameExternalAPI(GameType gameType, boolean isFirst) throws FunctionalException {
        ExternalAPI externalAPI = externalAPIProperties.getAllApis().get(externalAPIProperties.getSelected());
        String serverURL = externalAPI.getUrl();
        switch (externalAPIProperties.getSelected()) {
            case "group-e":
                serverURL += "/morpion/initvsAI" + gameType.equals(GameType.PVE) + "&starter=" + isFirst;
                PlayerE playerE = new PlayerE("Equipe popo", isFirst ? "x_value" : "o_value");
                HttpEntity<PlayerE> requestEntityE = new HttpEntity<>(playerE);
                ResponseEntity<GameE> responseE = http.exchange(serverURL, HttpMethod.POST, requestEntityE, GameE.class);
                switch (responseE.getStatusCode().value()) {
                    case 200 -> {
                    }
                    case 400 -> throw new FunctionalException(FunctionalRule.REQUEST_0001);
                    case 500 -> throw new FunctionalException(FunctionalRule.SERVER_0001);
                }
                break;
            case "group-a":
                serverURL += "/init?starts=" + isFirst;
                ResponseEntity<String> responseA = http.exchange(serverURL, HttpMethod.POST, HttpEntity.EMPTY, String.class);
                switch (responseA.getStatusCode().value()) {
                    case 200 -> {
                    }
                    case 400 -> throw new FunctionalException(FunctionalRule.REQUEST_0001);
                }
                break;
            case "group-d":
                serverURL += "/api/start?isFirst=" + isFirst + "&isVersusAI=" + gameType.equals(GameType.PVE);
                ResponseEntity<StartResponse> responseD = http.exchange(serverURL, HttpMethod.POST, null, StartResponse.class);
                if(responseD.getBody() == null || !responseD.getBody().isStart())
                    throw new FunctionalException(FunctionalRule.REQUEST_0001);
                break;
            default:
                break;
        }
    }

    /**
     * Make a player join the specified room
     *
     * @param roomCode   game room code
     * @param playerUUID the player uuid to join (create if not exists)
     * @return the game of the roomCode
     * @throws FunctionalException if the game isn't found.
     */
    public Game joinGame(String roomCode, String playerUUID) throws FunctionalException {
        Game game = findByRoomCode(roomCode);

        Player player = players.getOrDefault(playerUUID, null);
        if (player == null) {
            player = registerPlayer("guest");
        }
        addPlayer(game, player);
        return game;
    }

    public void quitGame(String roomCode, String playerUUID) throws FunctionalException {
        Game game = findByRoomCode(roomCode);

        Player player = getPlayerByPlayerUUID(playerUUID);

        if (!game.getPlayer1().equals(player) && !game.getPlayer2().equals(player)) {
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

    private Player getPlayerByPlayerUUID(String playerUUID) throws FunctionalException {
        Player player = players.getOrDefault(playerUUID, null);
        if (player == null) {
            throw new FunctionalException(
                    FunctionalRule.PLAYER_0001
            );
        }
        return player;
    }

    public void deleteGame(String roomCode) throws FunctionalException {
        deleteExternalApi();
        Game game = findByRoomCode(roomCode);

        games.remove(roomCode);
        logActionService.deleteAllByGameUUID(roomCode);
    }

    private void deleteExternalApi() throws FunctionalException {
        ExternalAPI externalAPI = externalAPIProperties.getAllApis().get(externalAPIProperties.getSelected());
        String serverURL = externalAPI.getUrl();
        switch (externalAPIProperties.getSelected()) {
            case "group-e":
                serverURL += "/morpion/quit";
                ResponseEntity<String> responseE = http.exchange(serverURL, HttpMethod.POST, HttpEntity.EMPTY, String.class);
                switch (responseE.getStatusCode().value()) {
                    case 200 -> {
                    }
                    case 400 -> throw new FunctionalException(FunctionalRule.REQUEST_0001);
                    case 500 -> throw new FunctionalException(FunctionalRule.SERVER_0001);
                }
                break;
            case "group-d":
                serverURL += "/api/quit";
                ResponseEntity<QuitResponse> responseD = http.exchange(serverURL, HttpMethod.POST, null, QuitResponse.class);
                if(responseD.getBody() == null || !responseD.getBody().isQuit())
                    throw new FunctionalException(FunctionalRule.GAME_0001);
                break;
            default:
                break;
        }
    }

    public Game findByRoomCode(String roomCode) throws FunctionalException {
        Game game = games.getOrDefault(roomCode, null);
        if (Objects.isNull(game)) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0001
            );
        }
        return game;
    }

    private void addPlayer(Game game, Player p) throws FunctionalException {
        if (game.getPlayer1() == null) {
            if (game.getCurrentPlayerId().isEmpty()) {
                game.setCurrentPlayerId(p.getUuid());
            }
            game.setPlayer1(p);
            return;
        }

        if (!game.getPlayer1().equals(p) && game.getPlayer2() == null) {
            if (game.getCurrentPlayerId().isEmpty()) {
                game.setCurrentPlayerId(p.getUuid());
            }
            game.setPlayer2(p);
            return;
        }

        if (game.getPlayer1().equals(p) || game.getPlayer2().equals(p)) {
            return;
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

    public Player registerPlayer(String username, @Nullable String playerUUID) {
        if (playerUUID == null) return registerPlayer(username);
        var player = new Player(playerUUID, username);
        players.put(player.getUuid(), player);
        return player;
    }

    /**
     * Check if a move is possible in the grid of the game
     *
     * @param roomCode current roomCode of the game
     * @param posI     position i of the move
     * @param posJ     position j of the move
     * @return true if it's possible
     */
    public boolean isAMovePossible(String roomCode, String playerUUID, int posI, int posJ) throws FunctionalException {
        Game game = findByRoomCode(roomCode);
        if (game.isFinished()) return false;

        positionIsCorrect(posI, posJ);
        if (!playerUUIDisCoherent(playerUUID, game)) return false;
        String subGridUuid = getTheSubGridUuid(posI, posJ, game);

        int i = posI % GameConstants.GRID_SIZE;
        int j = posJ % GameConstants.GRID_SIZE;

        boolean gameIsFull = game.getPlayer1() != null && game.getPlayer2() != null;
        boolean cellIsEmpty = cellIsEmpty(game, i, j, subGridUuid);
        boolean subGridPlayable = subGridPlayable(game, subGridUuid);
        boolean inTheSubGrid = inTheSubGrid(game, subGridUuid);
        boolean playerIsCorrect = playerUUID.equals(game.getCurrentPlayerId());
        return gameIsFull && cellIsEmpty && subGridPlayable && inTheSubGrid && playerIsCorrect;
    }

    private void positionIsCorrect(int posI, int posJ) throws FunctionalException {
        if (posI < 0 || posJ < 0 || posI >= GameConstants.GRID_SIZE * GameConstants.GRID_SIZE || posJ >= GameConstants.GRID_SIZE * GameConstants.GRID_SIZE) {
            throw new FunctionalException(
                    FunctionalRule.GAME_0005
            );
        }
    }

    private boolean playerUUIDisCoherent(String playerUUID, Game game) {
        if (game.getPlayer1() != null && game.getPlayer1().getUuid().equals(playerUUID))
            return true;

        return game.getPlayer2() != null && game.getPlayer2().getUuid().equals(playerUUID);
    }

    private String getTheSubGridUuid(int i, int j, Game game) {
        int posI = i / GameConstants.GRID_SIZE;
        int posJ = j / GameConstants.GRID_SIZE;
        return game.getGrid().getSubgrids()[posI][posJ].getUuid();
    }

    /**
     * Look if a cell is empty
     *
     * @param game        current game
     * @param i           position i of the move
     * @param j           position j of the move
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
     *
     * @param game        the current game
     * @param subGridUuid the subgrid's uuid we want to check
     * @return true if the subGrid is playable
     */
    private boolean subGridPlayable(Game game, String subGridUuid) {
        Subgrid subgrid = getSubGrid(game, subGridUuid);
        assert subgrid != null;
        return subgrid.isPlayable();
    }

    /**
     * Check if a subGrid is full
     *
     * @param subgrid subgrid to check
     * @return true if the subGrid is full
     */
    private boolean subGridIsFull(Subgrid subgrid) {
        CellStatus[][] cells = subgrid.getCells();
        for (int i = 0; i < GameConstants.SUBGRID_SIZE; i++) {
            for (int j = 0; j < GameConstants.SUBGRID_SIZE; j++) {
                if (cells[i][j] == CellStatus.EMPTY) return false;
            }
        }
        return true;
    }

    /**
     * Check if our subGrid is the actual subGrid we need to play in
     * (check si notre subgrid est celle où on doit jouer)
     *
     * @param game        the current game
     * @param subGridUuid the subgrid's uuid we want to make a move
     * @return true
     */
    private boolean inTheSubGrid(Game game, String subGridUuid) {
        if (game.getSubgridToPlayId() == null) return true;
        return game.getSubgridToPlayId().equals(subGridUuid);
    }

    private Subgrid getSubGrid(Game game, String subGridUuid) {
        Subgrid[][] subGrids = game.getGrid().getSubgrids();
        for (Subgrid[] subgrid : subGrids) {
            for (Subgrid sub : subgrid) {
                if (sub.getUuid().equals(subGridUuid)) {
                    return sub;
                }
            }
        }
        return null;
    }

    /**
     * @param roomCode current roomCode of the game
     * @param posI     position i of the move
     * @param posJ     position j of the move
     * @return si le jeu est fini ou non
     */
    public boolean playAMove(String roomCode, String playerUUID, int posI, int posJ) throws FunctionalException {
        Game game = findByRoomCode(roomCode);
        String subGribUuid = getTheSubGridUuid(posI, posJ, game);
        Subgrid subgrid = getSubGrid(game, subGribUuid);
        assert subgrid != null;

        int i = posI % GameConstants.GRID_SIZE;
        int j = posJ % GameConstants.GRID_SIZE;
        subgrid.getCells()[i][j] = game.getCurrentSymbol();

        if (game.getCurrentSymbol().equals(CellStatus.X)) {
            game.setCurrentSymbol(CellStatus.O);
        } else if (game.getCurrentSymbol().equals(CellStatus.O)) {
            game.setCurrentSymbol(CellStatus.X);
        }

        if (playerUUID.equals(game.getPlayer1().getUuid())) {
            game.setCurrentPlayerId(game.getPlayer2().getUuid());
        } else if (playerUUID.equals(game.getPlayer2().getUuid())) {
            game.setCurrentPlayerId(game.getPlayer1().getUuid());
        }

        logActionService.saveLogAction(
                "",
                game.getRoomCode(),
                (long) posI,
                (long) posJ
        );

        if (subGridIsFull(subgrid)) {
            subgrid.setPlayable(false);
        }
        if (isSubgridFinished(subgrid)) {
            calculateTheSubGridToPlay(game, i, j);
            return isGameFinished(game);
        } else {
            calculateTheSubGridToPlay(game, i, j);
            return false;
        }
    }

    private void calculateTheSubGridToPlay(Game game, int i, int j) {
        Subgrid subgrid = game.getGrid().getSubgrids()[i][j];
        if (subgrid.isPlayable()) {
            game.setSubgridToPlayId(subgrid.getUuid());
        } else {
            game.setSubgridToPlayId(null);
        }
    }

    private boolean isSubgridFinished(Subgrid subgrid) {
        CellStatus[][] cellStatus = subgrid.getCells();
        for (int i = 0; i < 3; i++) {
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
        if (isTheSameCellsForDiagonale(cellStatus)) {
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

        for (int i = 0; i < subgrids.length; i++) {
            for (int j = 0; j < subgrids[0].length; j++) {
                gridToCells[i][j] = subgrids[i][j].getWinner();
            }
        }

        for (int i = 0; i < 3; i++) {
            if (isTheSameCells(i, true, gridToCells)) {
                game.setFinished(true);
                setTheWinner(gridToCells[0][i], game);
                return true;
            } else if (isTheSameCells(i, false, gridToCells)) {
                game.setFinished(true);
                setTheWinner(gridToCells[i][0], game);
                return true;
            }
        }
        if (isTheSameCellsForDiagonale(gridToCells)) {
            game.setFinished(true);
            setTheWinner(gridToCells[1][1], game);
            return true;
        }

        return isWholeGridNotPlayable(game); // if the grid is full, the game is finished with no winner (null)
    }

    private boolean isWholeGridNotPlayable(Game game) {
        Subgrid[][] subgrids = game.getGrid().getSubgrids();
        for (int i = 0; i < GameConstants.GRID_SIZE; i++) {
            for (int j = 0; j < GameConstants.GRID_SIZE; j++) {
                if (subgrids[i][j].isPlayable()) return false;
            }
        }
        game.setFinished(true);
        return true;
    }

    private void setTheWinner(CellStatus cellStatus, Game game) {
        if (cellStatus == CellStatus.X) {
            game.setWinner(game.getPlayer1());
        } else if (cellStatus == CellStatus.O) {
            game.setWinner(game.getPlayer2());
        }
    }

    private static GridDTO getGridDTO(Game game, ActionDTO action) {
        int row = action.getI() / GameConstants.GRID_SIZE;
        int column = action.getJ() / GameConstants.GRID_SIZE;
        int childRow = action.getI() % GameConstants.GRID_SIZE;
        int childColumn = action.getJ() % GameConstants.GRID_SIZE;
        String value = game.getCurrentSymbol() == CellStatus.X ? "x_value" : "o_value";
        return new GridDTO(row, column, childRow, childColumn, value);
    }

    public void moveIA(Game game, String roomCode) {
        if (game.getCurrentPlayerId().equals(game.getPlayer2().getUuid())) {
            Player ia = game.getPlayer2();
            String iaUUID = ia.getUuid();

            int[] posMove = ia.iaMakeAMove(game);
            try {
                StompSession stompSession = stompClient.connectAsync("http://localhost:8080/data-info", stompSessionHandler).get();
                ActionDTO actionDTO = new ActionDTO(posMove[0], posMove[1], iaUUID);
                stompSession.send("/app/send-move/%s".formatted(roomCode), actionDTO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToExternalAPI(Game game, ActionDTO action) throws FunctionalException {
        if (!game.getGameType().equals(GameType.PVP_ONLINE))
            return;
        ExternalAPI externalAPI = externalAPIProperties.getAllApis().get(externalAPIProperties.getSelected());
        String serverURL = externalAPI.getUrl();
        switch (externalAPIProperties.getSelected()) {
            case "group-e":
                HttpEntity<GridDTO> httpEntity = new HttpEntity<>(getGridDTO(game, action));
                try {
                    http.postForObject("%s/morpion/play".formatted(externalAPI.getUrl()), httpEntity, GridDTO.class);
                } catch (RestClientException e) {
                    throw new FunctionalException(
                            FunctionalRule.GAME_0007
                    );
                }
                break;
            case "group-a":
                GridDTO gridDTOA = getGridDTO(game, action);
                String player = game.getCurrentSymbol() == CellStatus.X ? "X" : "O";
                serverURL += "/play?positionX=" + gridDTOA.getColumn() + "&positionY=" + gridDTOA.getRow() +
                        "&positionx=" + gridDTOA.getChildcolumn() + "&positiony=" + gridDTOA.getRow() + "&player=" + player;
                ResponseEntity<String> responseA = http.exchange(serverURL, HttpMethod.POST, HttpEntity.EMPTY, String.class);
                switch (responseA.getStatusCode().value()) {
                    case 200 -> {
                    }
                    case 400 -> throw new FunctionalException(FunctionalRule.REQUEST_0001);
                }
                break;
            case "group-d":
                GridDTO gridDTOD = getGridDTO(game, action);
                serverURL += "/api/send?bigX=" + gridDTOD.getColumn() +
                        "&bigY=" + gridDTOD.getRow() +
                        "&littleX=" + gridDTOD.getChildcolumn() +
                        "&littleY=" + gridDTOD.getChildrow();
                break;
            default:
                break;
        }
    }

}
