import { isEqual, set } from "lodash";
import { GameStopState } from "../core/GameStopStore";
import { useDispatch, useSelector } from "react-redux";
import { GameProps } from "./GameProps";
import { useEffect, useState } from "react";
import { useHistory } from "react-router";
import { useGetGamesQuery } from "./GameApi";
import { getErrorMessage, getLogger } from "../core";
import { clearToken } from "../auth/AuthSlice";
import { setError, setLoadingGames } from "./GameSlice";
import { FetchBaseQueryError } from "@reduxjs/toolkit/dist/query/react";
import { SerializedError } from "@reduxjs/toolkit";

const log = getLogger("useGames");

export function useGames() {
  const { loadingGames } = useSelector(
    (state: GameStopState) => state.game,
    isEqual
  );
  const { isAuthenticated } = useSelector(
    (state: GameStopState) => state.auth,
    isEqual
  );

  const dispatch = useDispatch();
  const history = useHistory();

  const [games, setGames] = useState<GameProps[]>([]);
  const { data, error, isLoading, refetch } = useGetGamesQuery(undefined, {
    skip: !isAuthenticated,
  });

  // Reset state on logout
  useEffect(() => {
    if (isAuthenticated && loadingGames) {
      refetch();
    }
  }, [isAuthenticated, loadingGames]);

  useEffect(() => {
    let mounted = true;

    log("useEffect", data);
    const handleError = (error: FetchBaseQueryError | SerializedError) => {
      const errorMessage = getErrorMessage(error, "Failed to fetch games!");
      if ((error as FetchBaseQueryError).status === 401) {
        dispatch(clearToken());
        history.push("/gamestop/login");
        return;
      }
      dispatch(setError({ type: "fetchError", message: errorMessage }));
    };

    if (error && mounted) {
      handleError(error);
      dispatch(setLoadingGames(false));
      return;
    }

    if (!isLoading && data && mounted) {
      setGames(data);
      dispatch(setLoadingGames(false));
      return;
    }

    return () => {
      mounted = false;
      dispatch(setLoadingGames(false));
    };
  }, [data, error, isLoading]);

  return { games };
}
