import Grid, {CellStatus} from "./grid.model.ts";
import Player from "./player.model.ts";

export default interface Game {
   roomCode: string;
   grid: Grid;

   player1?: Player;
   player2?: Player;
   winner: string;
   currentPlayerId: string;
   subgridToPlayId: string;
   currentSymbol: CellStatus;
   started: boolean;
   finished: boolean;
   gameType: GameType;
}

export enum GameType {
   PVP_LOCAL = "PVP_LOCAL", // "Joueur contre joueur en local"
   PVE = "PVE", // "Joueur contre IA"
   PVP_ONLINE = "PVP_ONLINE", // "Joueur contre joueur en ligne"
}