import { set } from "lodash";
import { GamePhoto } from "./usePhoto";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface PhotoState {
  photos: GamePhoto[];
  loadingPhotos: boolean;
  isFirstLoad: boolean;
}

const initialState: PhotoState = {
  photos: localStorage.getItem("photos")
    ? JSON.parse(localStorage.getItem("photos")!)
    : [],
  loadingPhotos: true,
  isFirstLoad: true,
};

const photoSlice = createSlice({
  name: "photo",
  initialState,
  reducers: {
    setPhotosSlice: (state, action: PayloadAction<GamePhoto[]>) => {
      state.photos = action.payload;
    },
    setLoadingPhotos: (state, action: PayloadAction<boolean>) => {
      state.loadingPhotos = action.payload;
    },
    setIsFirstLoad: (state, action: PayloadAction<boolean>) => {
      state.isFirstLoad = action.payload;
    },
  },
});

export const { setPhotosSlice, setLoadingPhotos, setIsFirstLoad } =
  photoSlice.actions;
export default photoSlice.reducer;
