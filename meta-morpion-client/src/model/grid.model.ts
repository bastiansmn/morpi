export default interface Grid {
   uuid: string;
   subgrids: Subgrid[];
}

export interface Subgrid {
   uuid: string;
   cells: CellStatus[];
   playable: boolean;
   winner: CellStatus;
}

export enum CellStatus {
   EMPTY = "",
   X = "X",
   O = "O"
}