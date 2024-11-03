import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseUrl, getLogger } from "../core";
import { GameProps } from "./GameProps";

const log = getLogger("GameStopApi");

export interface GetGamesRequest {
  username: string;
  page: number;
  title?: string;
}

export const gameStopApi = createApi({
  reducerPath: "gameStopApi",
  baseQuery: fetchBaseQuery({ baseUrl: `${baseUrl}/gamestop/api` }),
  tagTypes: ["Game"],
  endpoints: (builder) => ({
    getGames: builder.query<GameProps[], GetGamesRequest>({
      query: (getGamesInfo) => ({
        url: "games",
        method: "POST",
        body: getGamesInfo,
      }),
      providesTags: ["Game"],
      onCacheEntryAdded: async (
        arg,
        { updateCachedData, cacheDataLoaded, cacheEntryRemoved }
      ) => {
        const ws = new WebSocket(`${baseUrl.replace("http", "ws")}/ws`);
        try {
          // Waiting for the cache to be loaded
          await cacheDataLoaded;

          const listener = (message: MessageEvent) => {
            log("Received WS message", message);
            const data = JSON.parse(message.data);
            if (data.type === "UPDATE_GAME") {
              updateCachedData((draft) => {
                const idx = draft.findIndex((game) => game._id === data.tag);
                if (idx !== -1) {
                  draft[idx] = data.data;
                }
              });
            } else if (data.type === "SAVE_GAME") {
              updateCachedData((draft) => {
                draft.push(data.data);
              });
            }
          };
          ws.addEventListener("message", listener);
        } catch (error) {
          // If the cache loading fails, close the WebSocket connection
          await cacheEntryRemoved;

          log("Error in cache loading", error);
          ws.close();
        }
      },
    }),
    getGame: builder.query<GameProps, string>({
      query: (id) => `games/${id}`,
      providesTags: ["Game"],
    }),
    saveGame: builder.mutation<string, GameProps>({
      query: (game) => ({
        url: "games",
        method: "POST",
        body: game,
      }),
      invalidatesTags: ["Game"],
    }),
    updateGame: builder.mutation<GameProps, GameProps>({
      query: (game) => ({
        url: "games",
        method: "PUT",
        body: game,
      }),
      invalidatesTags: ["Game"],
    }),
  }),
});

export const {
  useGetGamesQuery,
  useGetGameQuery,
  useSaveGameMutation,
  useUpdateGameMutation,
} = gameStopApi;
