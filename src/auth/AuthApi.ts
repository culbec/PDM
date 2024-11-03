import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseUrl, getLogger } from "../core";

const log = getLogger("AuthApi");

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LogoutRequest {
  username: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export const authApi = createApi({
  reducerPath: "authApi",
  baseQuery: fetchBaseQuery({
    baseUrl: `${baseUrl}/gamestop/api/auth`,
    credentials: "include",
  }),
  endpoints: (builder) => ({
    login: builder.mutation<AuthResponse, LoginRequest>({
      query: (userInfo) => ({
        url: "login",
        method: "POST",
        body: userInfo,
        validateStatus: (response, result) => {
          log("login", response, result);
          return response.status === 200 && result.token !== undefined;
        },
      }),
    }),
    logout: builder.mutation<void, LogoutRequest>({
      query: (userInfo) => ({
        url: "logout",
        method: "POST",
        body: userInfo,
        validateStatus: (response) => {
          log("logout", response);
          return response.status === 200;
        },
      }),
    }),
    register: builder.mutation<AuthResponse, RegisterRequest>({
      query: (userInfo) => ({
        url: "register",
        method: "POST",
        body: userInfo,
        validateStatus: (response, result) => {
          log("register", response, result);
          return response.status === 200 && result.token !== undefined;
        },
      }),
    }),
  }),
});

export const { useLoginMutation, useLogoutMutation, useRegisterMutation } =
  authApi;
