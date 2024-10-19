import { configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import { gameStopApi } from "./GameStopApi";

export const gameStore = configureStore({
    reducer: {
        [gameStopApi.reducerPath]: gameStopApi.reducer,
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(gameStopApi.middleware),
});

setupListeners(gameStore.dispatch);