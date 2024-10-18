import React, { useCallback, useEffect, useReducer } from 'react';
import PropTypes from 'prop-types';
import { getLogger } from '../core';
import { GameProps } from './GameProps';
import { addGame, getGames, updateGame } from './GameApi';

const log = getLogger('GameProvider');

type SaveGameFn = (game: GameProps) => Promise<any>;

const FETCH_GAMES_STARTED = 'FETCH_GAMES_STARTED';
const FETCH_GAMES_SUCCEEDED = 'FETCH_GAMES_SUCCEEDED';
const FETCH_GAMES_FAILED = 'FETCH_GAMES_FAILED';

const SAVE_GAME_STARTED = 'SAVE_GAME_STARTED';
const SAVE_GAME_SUCCEEDED = 'SAVE_GAME_SUCCEEDED';
const SAVE_GAME_FAILED = 'SAVE_GAME_FAILED';

export interface GameState {
    games?: GameProps[],
    game?: GameProps,
    fetching: boolean,
    fetchingError?: Error | null,
    saving: boolean,
    savingError?: Error | null,
    saveGame?: SaveGameFn,
}

const initialState: GameState = {
    fetching: false,
    saving: false,
}

interface ActionProps {
    type: string,
    payload?: any,
}

export const GameContext = React.createContext<GameState>(initialState);

interface GameProviderProps {
    children: PropTypes.ReactNodeLike,
}

const reducer: (state: GameState, action: ActionProps) => GameState =
    (state, { type, payload }) => {
        switch (type) {
            case FETCH_GAMES_STARTED:
                return { ...state, fetching: true, fetchingError: null };
            case FETCH_GAMES_SUCCEEDED:
                return { ...state, games: payload.games, fetching: false };
            case FETCH_GAMES_FAILED:
                return { ...state, fetchingError: payload.error, fetching: false };
            case SAVE_GAME_STARTED:
                return { ...state, saving: true, savingError: null };
            case SAVE_GAME_SUCCEEDED: {
                const games = [...(state.games || [])];
                const game = payload.game;
                const idx = games.findIndex(item => item._id === game._id);

                if (idx === -1) {
                    games.splice(0, 0, game);
                } else {
                    games[idx] = game;
                }
                return { ...state, games, saving: false };
            }
            case SAVE_GAME_FAILED:
                return { ...state, savingError: payload.error, saving: false };
            default:
                return state;
        }
    }

export const GameProvider: React.FC<GameProviderProps> = ({ children }) => {
    const [state, dispatch] = useReducer(reducer, initialState);
    const { games, fetching, fetchingError, saving, savingError } = state

    useEffect(getGamesEffect, []);

    const saveGame = useCallback<SaveGameFn>(saveGameCallback, []);
    const value = { games, fetching, fetchingError, saving, savingError, saveGame };

    log('returns');
    return (
        <GameContext.Provider value={value}>
            {children}
        </GameContext.Provider>
    );

    function getGamesEffect() {
        let cancelled = false;
        fetchGames();

        return () => {
            cancelled = true;
        }

        async function fetchGames() {
            try {
                log('fetchGames started');
                dispatch({ type: FETCH_GAMES_STARTED });

                const games = await getGames();
                log('fetchGames successful!');

                if (!cancelled) {
                    dispatch({ type: FETCH_GAMES_SUCCEEDED, payload: { games } });
                }
            } catch (error) {
                log('fetchGames failed!');
                if (!cancelled) {
                    dispatch({ type: FETCH_GAMES_FAILED, payload: { error } });
                }
            }
        }
    }

    async function saveGameCallback(game: GameProps) {
        try {
            log('saveGame started');
            dispatch({ type: SAVE_GAME_STARTED });

            const savedGame = await (game._id ? updateGame(game) : addGame(game));
            log('saveGame successful!');
            dispatch({ type: SAVE_GAME_SUCCEEDED, payload: { game: savedGame } });
        } catch (error) {
            log('saveGame failed!');
            dispatch({ type: SAVE_GAME_FAILED, payload: { error } })
        }
    }
};

