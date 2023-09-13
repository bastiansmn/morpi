import {CellStatus} from "./grid.model.ts";

export interface AfterMoveState {
    type: 'AFTER_MOVE';
    gameFinished: boolean;
    i: number;
    j: number;
    playerUUID: string;
    nextPlayerUUID: string;
    nextSymbol: CellStatus;
    subgridToPlayId: string;
    winnerUUID: string;
    roomCode: string;
}