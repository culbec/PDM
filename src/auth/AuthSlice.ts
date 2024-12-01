import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { gameApi } from "../game/GameApi";

interface AuthState {
  userId: string | null;
  isAuthenticated: boolean;
  authError: string | null;
}

const initialState: AuthState = {
  userId: localStorage.getItem("userId"),
  isAuthenticated: localStorage.getItem("token") !== null,
  authError: null,
};

const validateAndSetToken = (token: string | null): boolean => {
  if (!token) {
    localStorage.removeItem("token");
    return false;
  }
  localStorage.setItem("token", token);
  return true;
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setUserId: (state, action: PayloadAction<string>) => {
      state.userId = action.payload;
      localStorage.setItem("userId", action.payload);
    },
    setToken: (state, action: PayloadAction<string>) => {
      state.isAuthenticated = validateAndSetToken(action.payload);
    },
    clearToken: (state) => {
      state.userId = initialState.userId;
      state.isAuthenticated = false;
      localStorage.removeItem("token");
      localStorage.removeItem("userId");
      gameApi.util.invalidateTags(["GameList"]);
    },
    setAuthError: (
      state,
      action: PayloadAction<string | { message: string }>
    ) => {
      state.authError =
        typeof action.payload === "string"
          ? action.payload
          : action.payload.message;
    },
    clearAuthError: (state) => {
      state.authError = initialState.authError;
    },
  },
});

export const { setUserId, setToken, clearToken, setAuthError, clearAuthError } =
  authSlice.actions;
export default authSlice.reducer;
