import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { GameProps } from "./GameProps";

interface GameState {
  selectedGame: GameProps | null;
  uiErrors: {
    fetchError: string | undefined;
    saveError: string | undefined;
  };
  infoMessage: string | undefined;
  notification: string | undefined;
}

const initialState: GameState = {
  selectedGame: null,
  uiErrors: {
    fetchError: undefined,
    saveError: undefined,
  },
  infoMessage: undefined,
  notification: undefined,
};

const gameSlice = createSlice({
  name: "game",
  initialState,
  reducers: {
    setSelectedGame: (state, action: PayloadAction<GameProps | null>) => {
      if (state.selectedGame?._id !== action.payload?._id) {
        state.selectedGame = action.payload;
      }
    },
    setError: (
      state,
      action: PayloadAction<{
        type: keyof GameState["uiErrors"];
        message: string | undefined;
      }>
    ) => {
      const { type, message } = action.payload;
      if (state.uiErrors[type] !== message) {
        state.uiErrors[type] = message;
      }
    },
    clearErrors: (state) => {
      if (state.uiErrors.fetchError || state.uiErrors.saveError) {
        state.uiErrors = initialState.uiErrors;
      }
    },
    setInfoMessage: (state, action: PayloadAction<string | undefined>) => {
      if (state.infoMessage !== action.payload) {
        state.infoMessage = action.payload;
      }
    },
    clearInfoMessage: (state) => {
      if (state.infoMessage) {
        state.infoMessage = initialState.infoMessage;
      }
    },
    setNotification: (state, action: PayloadAction<string | undefined>) => {
      if (state.notification !== action.payload) {
        state.notification = action.payload;
      }
    },
    clearNotification: (state) => {
      if (state.notification) {
        state.notification = initialState.notification;
      }
    },
  },
});

export const {
  setSelectedGame,
  setError,
  clearErrors,
  setInfoMessage,
  clearInfoMessage,
  setNotification,
  clearNotification,
} = gameSlice.actions;

export default gameSlice.reducer;
