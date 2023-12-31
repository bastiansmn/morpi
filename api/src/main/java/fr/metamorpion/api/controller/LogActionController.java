package fr.metamorpion.api.controller;

import fr.metamorpion.api.model.LogAction;
import fr.metamorpion.api.service.LogActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class LogActionController {

    private final LogActionService logActionService;

    @Operation(
            summary = "Get all logs after date",
            description = "Get all logs after the specified date for the specified room code"
    )
    @ApiResponse(responseCode = "200")
    @GetMapping("after/{roomCode}")
    public ResponseEntity<Collection<LogAction>> getLogsAfter(@PathVariable("roomCode") String roomCode, @Parameter(description = "e.g : 2023-09-12T11:25:03") @RequestParam("ts") LocalDateTime timestamp) {
        return ResponseEntity.ok(logActionService.getLogsAfter(timestamp, roomCode));
    }

    @Operation(
            summary = "Get logs of room",
            description = "Get all logs of room"
    )
    @ApiResponse(responseCode = "200")
    @GetMapping("{roomCode}")
    public ResponseEntity<Collection<LogAction>> getLogsOfRoom(@PathVariable("roomCode") String roomCode) {
        return ResponseEntity.ok(logActionService.getLogsOfRoom(roomCode));
    }

}
