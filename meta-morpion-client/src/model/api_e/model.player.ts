export default interface Player {
    playerName: string;
    gameValue: string;
}

export enum GameValue {
    x_value = "X",
    o_value = "O",
    none = ""
}