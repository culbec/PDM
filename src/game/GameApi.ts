import axios from "axios";
import { baseUrl, getLogger, withLogs } from "../core";
import { GameProps } from "./GameProps";

const log = getLogger("GameApi");
const gameApiUrl = `${baseUrl}/gamestop/api/games`;

const config = {
    headers: {
        "Content-Type": "application/json",
    },
}

export const getGames: () => Promise<GameProps[]> = () => {
    log("getGames");
    return withLogs<GameProps[]>(axios.get(gameApiUrl, config), "getGames");
}

export const getGame: (id: string) => Promise<GameProps> = (id) => {
    return withLogs<GameProps>(axios.get(`${gameApiUrl}/${id}`, config), "getGame");
}

export const addGame: (game: GameProps) => Promise<string> = (game) => {
    return withLogs(axios.post(gameApiUrl, game, config), "addGame");
}

export const updateGame: (game: GameProps) => Promise<undefined | string> = (game) => {
    return withLogs(axios.put(`${gameApiUrl}/${game._id}`, game, config), "updateGame");
}

export const removeGame: (game: GameProps) => Promise<undefined | string> = (game) => {
    return withLogs(axios.delete(`${gameApiUrl}/${game._id}`, config), "removeGame");
}