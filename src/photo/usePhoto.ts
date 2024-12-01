import { useCallback, useEffect, useRef, useState } from "react";
import { useCamera } from "./useCamera";
import { useFilesystem } from "../core/useFilesystem";
import { getErrorMessage, getLogger } from "../core";
import { useDispatch, useSelector } from "react-redux";
import useSync from "../core/useSync";
import { GameStopState } from "../core/GameStopStore";
import { setError } from "../game/GameSlice";
import { useGetPhotosForUserQuery } from "./PhotoApi";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";
import { SerializedError } from "@reduxjs/toolkit";
import { clearToken } from "../auth/AuthSlice";
import { useHistory } from "react-router";
import { isEqual, set } from "lodash";
import { setIsFirstLoad, setLoadingPhotos, setPhotosSlice } from "./PhotoSlice";

export interface GamePhoto {
  user_id: string;
  game_id: string;
  filepath: string;
  data: string;
}

const log = getLogger("usePhoto");

export function usePhoto() {
  const { photos, loadingPhotos, isFirstLoad } = useSelector(
    (state: GameStopState) => state.photo,
    isEqual
  );
  const { userId, isAuthenticated } = useSelector(
    (state: GameStopState) => state.auth,
    isEqual
  );
  const { getPhoto } = useCamera();
  const { readFile, writeFile, deleteFile } = useFilesystem();

  const dispatch = useDispatch();
  const { startSync } = useSync(dispatch);

  const history = useHistory();

  const { data, error, isLoading, refetch } = useGetPhotosForUserQuery(
    userId!,
    {
      skip: !isAuthenticated,
    }
  );

  const loadPhotos = useCallback(
    async (newPhotos: GamePhoto[]) => {
      dispatch(setLoadingPhotos(true));
      try {
        await Promise.all(
          newPhotos.map(async (photo) => {
            try {
              await readFile(photo.filepath);
            } catch (error: any) {
              await writeFile(photo.filepath, photo.data);
            }
          })
        );

        dispatch(setPhotosSlice(newPhotos));
      } catch (error: any) {
        log("loadPhotos", error);
        const errorMessage = getErrorMessage(error, "Failed to load photos");
        dispatch(setError({ type: "fetchError", message: errorMessage }));
      }
      dispatch(setLoadingPhotos(false));
    },
    [dispatch, readFile]
  );

  const handleError = (error: FetchBaseQueryError | SerializedError) => {
    const errorMessage = getErrorMessage(error, "Failed to fetch games!");
    if ((error as FetchBaseQueryError).status === 401) {
      dispatch(clearToken());
      history.push("/gamestop/login");
      return;
    }
    dispatch(setError({ type: "fetchError", message: errorMessage }));
  };

  // Reset state on init/login
  useEffect(() => {
    log("init useEffect");
    log("useEffect", isAuthenticated, loadingPhotos);
    if (isAuthenticated && isFirstLoad) {
      setLoadingPhotos(true);
      refetch();
    }
  }, [isAuthenticated, isFirstLoad]);

  // Load photos from server and store them into the filesystem
  useEffect(() => {
    let mounted = true;
    log("fetchPhotos effect");

    fetchPhotos();

    return () => {
      mounted = false;
      dispatch(setLoadingPhotos(false));
    };

    async function fetchPhotos() {
      if (!mounted || !isAuthenticated || !loadingPhotos) {
        dispatch(setLoadingPhotos(false));
        return;
      }

      if (error && mounted) {
        handleError(error);
        dispatch(setLoadingPhotos(false));
        return;
      }

      if (!isLoading && data && mounted) {
        log("going to load photos", data);
        await loadPhotos(data);
        dispatch(setIsFirstLoad(false));
        return;
      }
    }
  }, [data, error, isLoading, loadingPhotos]);

  const writePhoto = useCallback(
    async (photo: GamePhoto) => {
      try {
        await writeFile(photo.filepath, photo.data);
        const newPhotos = [photo, ...photos];

        dispatch(setPhotosSlice(newPhotos));
      } catch (error: any) {
        const errorMessage = getErrorMessage(error, "Failed to write photo");
        dispatch(setError({ type: "fetchError", message: errorMessage }));
      }
    },
    [writeFile, photos, dispatch]
  );

  const takePhoto = useCallback(
    async (game_id: string) => {
      log("takePhoto");

      const data = await getPhoto();
      const filepath = new Date().getTime() + ".jpeg";

      const photoData = data.base64String!;
      const photo = {
        user_id: userId!,
        game_id,
        filepath,
        data: photoData,
      };

      await writePhoto(photo);
      startSync({
        type: "save_photo",
        payload: photo,
      });

      return { user_id: userId, game_id, filepath, data: photoData };
    },
    [userId, getPhoto, writePhoto, startSync]
  );

  const deletePhoto = useCallback(
    async (photo: GamePhoto) => {
      // Check if photo is in the list of photos
      if (!photos.includes(photo)) {
        return;
      }

      // Removing the photo from the list of photos
      const newPhotos = photos.filter((p) => p !== photo);

      // Deleting the photo from the filesystem
      await deleteFile(photo.filepath);

      startSync({ type: "delete_photo", payload: photo });
      dispatch(setPhotosSlice(newPhotos));
    },
    [deleteFile, photos, dispatch, startSync]
  );

  return {
    takePhoto,
    writePhoto,
    deletePhoto,
  };
}
