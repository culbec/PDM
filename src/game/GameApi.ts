import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseUrl} from "../core";
import { GameProps } from "./GameProps";

const apiQuery = fetchBaseQuery({
  baseUrl: `${baseUrl}/gamestop/api`,
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

export const gameApi = createApi({
  reducerPath: "gameApi",
  baseQuery: apiQuery,
  tagTypes: ["GameList"],
  endpoints: (builder) => ({
    getGames: builder.query<GameProps[], void>({
      query: () => "games",
      providesTags: ["GameList"],
    }),
    getGame: builder.query<GameProps, string>({
      query: (id) => `games/${id}`,
      providesTags: ["GameList"],
    }),
    saveGame: builder.mutation<string, GameProps>({
      query: (game) => ({
        url: "games",
        method: "POST",
        body: game,
      }),
      invalidatesTags: ["GameList"],
    }),
    updateGame: builder.mutation<GameProps, GameProps>({
      query: (game) => ({
        url: "games",
        method: "PUT",
        body: game,
      }),
      invalidatesTags: ["GameList"],
    }),
  }),
});

export const {
  useGetGamesQuery,
  useGetGameQuery,
  useSaveGameMutation,
  useUpdateGameMutation,
} = gameApi;
