import { createApi } from "@reduxjs/toolkit/query/react";
import { GamePhoto } from "./usePhoto";
import { apiQuery } from "../core";

export const photoApi = createApi({
  reducerPath: "photoApi",
  baseQuery: apiQuery,
  tagTypes: ["Photos"],
  endpoints: (builder) => ({
    getPhotosForUser: builder.query<GamePhoto[], string>({
      query: (userId) => `photos/${userId}`,
      providesTags: ["Photos"],
    }),
    savePhoto: builder.mutation<void, GamePhoto>({
      query: (photo) => ({
        url: "photos",
        method: "POST",
        body: photo,
      }),
      invalidatesTags: ["Photos"],
    }),
    deletePhoto: builder.mutation<void, GamePhoto>({
      query: (photo) => ({
        url: `photos/${photo.filepath}`,
        method: "DELETE",
      }),
      invalidatesTags: ["Photos"],
    }),
  }),
});

export const {
  useGetPhotosForUserQuery,
  useSavePhotoMutation,
  useDeletePhotoMutation,
} = photoApi;
