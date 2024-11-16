import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { authApi } from "./AuthApi";
import { gameApi } from "../game/GameApi";

interface AuthState {
  isAuthenticated: boolean;
  authError: string | null;
}

const initialState: AuthState = {
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
    setToken: (state, action: PayloadAction<string>) => {
      state.isAuthenticated = validateAndSetToken(action.payload);
    },
    clearToken: (state) => {
      state.isAuthenticated = false;
      localStorage.removeItem("token");
      gameApi.util.invalidateTags(["GameList"]);
    },
    setAuthError: (state, action: PayloadAction<string | { message: string }>) => {
      state.authError = typeof action.payload === "string" ? action.payload : action.payload.message;
    },
    clearAuthError: (state) => {
      state.authError = initialState.authError;
    },
  },
});

export const { setToken, clearToken, setAuthError, clearAuthError } =
  authSlice.actions;
export default authSlice.reducer;
