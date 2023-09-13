import Player from "./model.player.ts";
import Grid from "./model.grid.ts";

export default interface Game {
    player1: Player,
    player2: Player,
    currentPlayer: Player,
    grid: Grid,
    finished: boolean,
    ready: boolean
}