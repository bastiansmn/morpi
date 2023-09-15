package fr.metamorpion.api.controller;

import fr.metamorpion.api.exception.ApiError;
import fr.metamorpion.api.exception.FunctionalException;
import fr.metamorpion.api.model.ActionDTO;
import fr.metamorpion.api.model.AfterMoveState;
import fr.metamorpion.api.model.Game;
import fr.metamorpion.api.model.GameType;
import fr.metamorpion.api.model.Player;
import fr.metamorpion.api.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Tag(name = "Game controller", description = "Game managing (create or join game, register player, ...")
@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @Value("${client-domain:}")
    private String clientDomain;

    @Operation(
            summary = "Get the list of current rooms",
            description = "Get the rooms that haven't started yet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @GetMapping("all")
    public ResponseEntity<Collection<Game>> getPendingGames() {
        return ResponseEntity.ok(gameService.getPendingGames());
    }

    @Operation(
            summary = "Get game by roomCode"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified game doesn't exist",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            )
    })
    @GetMapping("{roomCode}")
    public ResponseEntity<Game> getGameByRoomCode(@PathVariable("roomCode") String roomCode) throws FunctionalException {
        return ResponseEntity.ok(gameService.findByRoomCode(roomCode));
    }

    @Operation(
            summary = "Register a new user (optional)",
            description = "(Optional) Register the user according to the name and saves him to a list of players"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @PostMapping("register")
    public ResponseEntity<Player> registerPlayer(@RequestParam("username") String username, @RequestParam(value = "playerUUID", required = false) String playerUUID) {
        return ResponseEntity.ok(gameService.registerPlayer(username, playerUUID));
    }

    @Operation(
            summary = "Create a new room/game",
            description = "Creates a new room with the given player as admin (1st player - not to be confused with first player to play). Creates the user if he does not exist"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @PostMapping("create")
    public ResponseEntity<Game> createGame(
            @RequestParam("gameType") GameType gameType,
            @RequestParam(value = "playerUUID", required = false) String playerUUID,
            @RequestParam("firstToPlay") boolean isFirstToPlay,
            HttpServletRequest req
    ) throws FunctionalException {
        return ResponseEntity.ok(
                gameService.createGame(gameType, playerUUID, isFirstToPlay, req.getHeader("Host").startsWith(clientDomain))
        );
    }

    @Operation(
            summary = "Join a room",
            description = "Join a room with the room code associated. Creates the user if he does not exist"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(
                    responseCode = "400",
                    description = "User is already in game or the game is full",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified game doesn't exist",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            )
    })
    @PostMapping("join-room")
    public ResponseEntity<Game> joinRoom(
            @RequestParam("roomCode") String roomCode,
            @RequestParam(value = "playerUUID", required = false) String playerUUID
    ) throws FunctionalException {
        return ResponseEntity.ok(gameService.joinGame(roomCode, playerUUID));
    }

    @Operation(
            summary = "Quit a room",
            description = "Quit a room of the room code associated, delete the user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(
                    responseCode = "400",
                    description = "User not in game",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified game doesn't exist",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified player doesn't exists",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            )
    })
    @PostMapping("quit-room")
    public ResponseEntity<Void> quitRoom(
            @RequestParam("roomCode") String roomCode,
            @RequestParam("playerUUID") String playerUUID
    ) throws FunctionalException {
        gameService.quitGame(roomCode, playerUUID);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Send a move",
            description = "Send a move to the associated roomcode. The response specifies if the move could be played : if so, the player receives the updated game state, otherwise an error message."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Move was played. If the game is finished, the winner can be null/empty if it is a tie (otherwise the winner's UUID)"),
            @ApiResponse(
                    responseCode = "400",
                    description = "User not in game",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "This move is unauthorized",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified game doesn't exist",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified player doesn't exists",
                    content = { @Content(schema = @Schema(implementation = ApiError.class)) }
            )
    })
    @PostMapping("send-move")
    public ResponseEntity<Void> sendMove(
            @RequestParam("roomCode") String roomCode,
            @Parameter(description = "UUID of who is sending the move") @RequestParam("playerUUID") String playerUUID,
            @Parameter(description = "Absolute line number in the big grid") @RequestParam("i") int i,
            @Parameter(description = "Absolute column number in the big grid")@RequestParam("j") int j,
            HttpServletRequest req
    ) throws FunctionalException {
        gameService.sendMoveWS(roomCode, new ActionDTO(i, j, playerUUID, req.getHeader("Host").startsWith(clientDomain)));
        return ResponseEntity.noContent().build();
    }


    //////////////////////////
    /// WebSocket endpoints //
    //////////////////////////

    @Operation(
            summary = "Send a move",
            description = "Send a move through the websocket"
    )
    @MessageMapping("/send-move/{roomCode}")
    @SendTo("/room/{roomCode}")
    public AfterMoveState sendMoveWS(
            @DestinationVariable("roomCode") String roomCode,
            @RequestBody ActionDTO action
    ) throws FunctionalException {
        return gameService.sendMove(roomCode, action, action.isLocal());
    }

    @Operation(
            summary = "Leave room",
            description = "Leave room through the websocket"
    )
    @MessageMapping("/leave-room/{roomCode}")
    @SendTo("/room/{roomCode}")
    public void leaveRoomWS(
            @DestinationVariable("roomCode") String roomCode,
            @RequestBody ActionDTO action
    ) throws FunctionalException {
        gameService.quitGame(roomCode, action.getPlayerUUID());
    }

}
