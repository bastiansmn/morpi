package fr.metamorpion.api.controller;

import fr.metamorpion.api.exception.ApiError;
import fr.metamorpion.api.exception.FunctionalException;
import fr.metamorpion.api.exception.FunctionalRule;
import fr.metamorpion.api.model.AfterMoveState;
import fr.metamorpion.api.model.Game;
import fr.metamorpion.api.model.GameType;
import fr.metamorpion.api.model.Player;
import fr.metamorpion.api.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Operation(
            summary = "Get the list of current room",
            description = "Get the rooms that aren't started yet"
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
            summary = "Register a new user",
            description = "Register the user according to the name and saves him to the list"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @PostMapping("register")
    public ResponseEntity<Player> registerPlayer(@RequestParam("username") String username) {
        return ResponseEntity.ok(gameService.registerPlayer(username));
    }

    @Operation(
            summary = "Create a new room",
            description = "Creates a new room with the given player as admin (1st player). Creates the user if not exists"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @PostMapping("create")
    public ResponseEntity<Game> createGame(@RequestParam("gameType") GameType gameType, @RequestParam("playerUUID") String playerUUID) throws FunctionalException {
        return ResponseEntity.ok(gameService.createGame(gameType, playerUUID));
    }

    @Operation(
            summary = "Join a room",
            description = "Join a room with the room code associated. Creates the user if not exists"
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
            @RequestParam("playerUUID") String playerUUID
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
            description = "Send a move if it's possible"
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
    @PostMapping("send-move")
    public ResponseEntity<AfterMoveState> sendMove(
            @RequestParam("roomCode") String roomCode,
            @RequestParam("playerUUID") String playerUUID,
            @RequestParam("i") int i,
            @RequestParam("j") int j
    ) throws FunctionalException {
        boolean movePossible = gameService.isAMovePossible(roomCode, playerUUID, i, j);
        if (movePossible) {
            boolean gameFinished = gameService.playAMove(roomCode, playerUUID, i, j);
            return ResponseEntity.ok(new AfterMoveState(roomCode, playerUUID, i, j, gameFinished));
        } else {
            throw new FunctionalException(
                    FunctionalRule.GAME_0005
            );
        }
    }
}
