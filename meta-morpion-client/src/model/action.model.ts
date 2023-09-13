export interface AfterMoveState {
    type: 'AFTER_MOVE';
    gameFinished: boolean;
    i: number;
    j: number;
    playerUUID: string;
    roomCode: string;
}