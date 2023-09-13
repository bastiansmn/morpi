import ChildGrid from "./model.childgrid.ts";

export default interface Grid {
    childGrids: ChildGrid[][],
    winnerValue: string,
    completed: boolean
}