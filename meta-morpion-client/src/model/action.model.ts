export interface AfterMoveState {
    type: 'AFTER_MOVE';
    gameFinished: boolean;
    i: number;
    j: number;
    playerUUID: string;
    nextPlayerUUID: string;
    winnerUUID: string;
    roomCode: string;
}