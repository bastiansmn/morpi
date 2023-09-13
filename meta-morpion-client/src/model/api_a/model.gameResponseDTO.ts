export default interface GameResponseDTO {
    nextX: string,
    nextY: string,
    nextPlayer: string,
    isValid: boolean,
    hasWon: boolean,
    isFinished: boolean,
    gridCompleted: boolean,
    gridWon: boolean
}