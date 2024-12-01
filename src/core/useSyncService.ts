import { useEffect } from "react";
import useSync from "./useSync";
import { useDispatch } from "react-redux";
import { getLogger } from ".";
import useNetwork from "./useNetwork";

const log = getLogger("useSyncService");

const useSyncService = () => {
  const dispatch = useDispatch();
  const { networkStatus } = useNetwork();
  const { startSync } = useSync(dispatch);

  useEffect(() => {
    log("useEffect");

    if (networkStatus.isConnected) {
      startSync();
    }
  }, [networkStatus.isConnected, startSync]);
};

export default useSyncService;
