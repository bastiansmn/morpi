import {CellStatus} from "./grid.model.ts";
import Player from "./player.model.ts";

export interface AfterMoveState {
    type: 'AFTER_MOVE';
    gameFinished: boolean;
    i: number;
    j: number;
    playerUUID: string;
    nextPlayerUUID: string;
    nextSymbol: CellStatus;
    subgridToPlayId: string;
    completedSubgridId: string;
    finishedSubgrid: string;
    winner?: Player;
    roomCode: string;
}