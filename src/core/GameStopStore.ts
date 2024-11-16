import { configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import { authApi } from "../auth/AuthApi";
import { gameApi } from "../game/GameApi";
import authReducer from "../auth/AuthSlice";
import gameReducer from "../game/GameSlice";

export const gameStopStore = configureStore({
  reducer: {
    auth: authReducer,
    game: gameReducer,
    [authApi.reducerPath]: authApi.reducer,
    [gameApi.reducerPath]: gameApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(authApi.middleware)
      .concat(gameApi.middleware),
});

setupListeners(gameStopStore.dispatch);

export type GameStopState = ReturnType<typeof gameStopStore.getState>;
