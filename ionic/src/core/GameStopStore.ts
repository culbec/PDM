import { configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import { authApi } from "../auth/AuthApi";
import { gameApi } from "../game/GameApi";
import authReducer from "../auth/AuthSlice";
import gameReducer from "../game/GameSlice";
import photoReducer from "../photo/PhotoSlice";
import { photoApi } from "../photo/PhotoApi";

export const gameStopStore = configureStore({
  reducer: {
    auth: authReducer,
    game: gameReducer,
    photo: photoReducer,
    [authApi.reducerPath]: authApi.reducer,
    [gameApi.reducerPath]: gameApi.reducer,
    [photoApi.reducerPath]: photoApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(authApi.middleware)
      .concat(gameApi.middleware)
      .concat(photoApi.middleware),
});

setupListeners(gameStopStore.dispatch);

export type GameStopState = ReturnType<typeof gameStopStore.getState>;
