import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseUrl, getLogger } from "../core";

const log = getLogger("AuthApi");

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

const apiQuery = fetchBaseQuery({
  baseUrl: `${baseUrl}/gamestop/api/auth`,
  credentials: "same-origin",
  mode: "cors",
  prepareHeaders: (headers) => {
    const token = localStorage.getItem("token");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    return headers;
  },
});

export const authApi = createApi({
  reducerPath: "authApi",
  baseQuery: apiQuery,
  refetchOnMountOrArgChange: false,
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
    logout: builder.mutation<void, void>({
      query: () => ({
        url: "logout",
        method: "POST",
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
          return response.status === 201 && result.token !== undefined;
        },
      }),
    }),
  }),
});

export const { useLoginMutation, useLogoutMutation, useRegisterMutation } =
  authApi;
