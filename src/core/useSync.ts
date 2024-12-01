import { useCallback } from "react";
import useNetwork from "./useNetwork";
import { useSaveGameMutation, useUpdateGameMutation } from "../game/GameApi";
import { useHistory } from "react-router";
import { getErrorMessage, getLogger } from ".";
import { Dispatch, SerializedError } from "@reduxjs/toolkit";
import { setError, setInfoMessage } from "../game/GameSlice";
import { forEach } from "lodash";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";
import { GameProps } from "../game/GameProps";
import { clearToken } from "../auth/AuthSlice";
import {
  useDeletePhotoMutation,
  useSavePhotoMutation,
} from "../photo/PhotoApi";
import { GamePhoto } from "../photo/usePhoto";

interface SyncProps {
  type: "save_game" | "update_game" | "save_photo" | "delete_photo";
  payload: GameProps | GamePhoto;
}

const log = getLogger("useSync");

const useSync = (dispatch: Dispatch) => {
  const history = useHistory();
  const { networkStatus } = useNetwork();

  const [saveGame] = useSaveGameMutation();
  const [updateGame] = useUpdateGameMutation();
  const [savePhoto] = useSavePhotoMutation();
  const [deletePhoto] = useDeletePhotoMutation();

  const processSync: (
    syncProps: SyncProps,
    syncs: SyncProps[]
  ) => SyncProps[] = (syncProps, syncs) => {
    let { type, payload } = syncProps;

    // Add or update the existing sync operation
    if (syncs.length > 0) {
      syncs = syncs.reduce((acc: SyncProps[], sync: SyncProps) => {
        if ((sync.payload as GameProps)._id !== undefined) {
          sync.payload = sync.payload as GameProps;
          payload = payload as GameProps;
          if (sync.type === type && sync.payload._id === payload._id) {
            return [...acc, { type, payload }];
          }
          if (type === "save_game" && sync.payload.title === payload.title) {
            return acc;
          }
          return [...acc, sync];
        } else {
          sync.payload = sync.payload as GamePhoto;
          payload = payload as GamePhoto;
          if (
            sync.type === type &&
            sync.payload.filepath === payload.filepath
          ) {
            return [...acc, { type, payload }];
          }
          return [...acc, sync];
        }
      }, []);
    } else {
      syncs.push({ type, payload });
    }

    return syncs;
  };

  const startSync = useCallback(
    async (syncProps?: SyncProps) => {
      // Check for existing syncs and add new sync if necessary
      const syncsString = localStorage.getItem("syncs");
      let syncs: SyncProps[] = syncsString ? JSON.parse(syncsString) : [];

      log("synching", syncProps);
      log("networkStatus", networkStatus);

      // Check if there was provided a sync operation
      // If there wasn't, check for internet connection
      // and exit early if there is no internet
      if (!syncProps && !networkStatus.isConnected) {
        log("no internet -> skipping sync", syncs);
        return;
      }

      // Process the passed sync object
      if (syncProps) {
        syncs = processSync(syncProps, syncs);
      }

      // Skip sync if offline, by saving the processed sync
      // operations to local storage
      if (!networkStatus.isConnected || syncs.length === 0) {
        log("no internet or no syncs -> skipping sync", syncs);
        localStorage.setItem("syncs", JSON.stringify(syncs));
        return;
      }

      // Trigger sync when online
      log("there is internet -> processing syncs", syncs);
      // Clearing existing syncs -> we do not need them anymore
      localStorage.removeItem("syncs");

      const errors: string[] = [];
      const infos: string[] = [];

      try {
        await Promise.all(
          syncs.map(async (sync_: SyncProps) => {
            const { type, payload } = sync_;

            // Process the operation type
            // Log error if operation is unknown
            // Log error if operation fails
            // Exit if unauthorized
            try {
              switch (type) {
                case "save_game": {
                  await saveGame(payload as GameProps).unwrap();

                  log("saveGameSuccess");
                  infos.push("Game saved successfully!");
                  break;
                }
                case "update_game": {
                  await updateGame(payload as GameProps).unwrap();

                  log("updateGameSuccess");
                  infos.push("Game updated successfully!");
                  break;
                }
                case "save_photo": {
                  await savePhoto(payload as GamePhoto).unwrap();

                  log("savePhotoSuccess");
                  infos.push("Photo saved successfully!");
                  break;
                }
                case "delete_photo": {
                  await deletePhoto(payload as GamePhoto).unwrap();

                  log("deletePhotoSuccess");
                  infos.push("Photo deleted successfully!");
                  break;
                }
                default: {
                  log("unknownType", type);
                  errors.push("Unknown sync operation!");
                }
              }
            } catch (error: any) {
              log("error", error);

              if (
                (error as FetchBaseQueryError).status === 401 ||
                (error as SerializedError).code?.includes("401")
              ) {
                log("Unauthorized -> logging out");

                dispatch(clearToken());
                history.push("/gamestop/login");
                return;
              }

              errors.push(
                getErrorMessage(error, `Failed for type: '${type}'!`)
              );
            }
          })
        );

        // Dispatching errors and info messages
        forEach(errors, (error) => {
          dispatch(setError({ type: "saveError", message: error }));
        });
        forEach(infos, (info) => {
          dispatch(setInfoMessage(info));
        });
      } catch (error) {
        if (
          (error as FetchBaseQueryError).status === 401 ||
          (error as SerializedError).code?.includes("401")
        ) {
          log("Unauthorized -> logging out");

          dispatch(clearToken());
          history.push("/gamestop/login");
          return;
        }
      }
    },
    [networkStatus, saveGame, updateGame]
  );

  return { startSync };
};

export default useSync;
