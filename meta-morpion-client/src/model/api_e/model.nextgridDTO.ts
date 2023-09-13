import Player from "./model.player.ts";

export default interface NextGridDTO {
    row: number,
    column: number,
    player: Player,
    finished: boolean,
    lastChildFinished: string
}