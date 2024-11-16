import { useEffect } from "react";
import useSync from "./useSync";
import { useDispatch } from "react-redux";
import useNetwork from "./useNetwork";

const useSyncService = () => {
  const dispatch = useDispatch();
  const { networkStatus } = useNetwork();
  const { startSync } = useSync(dispatch);

  useEffect(() => {
    startSync();
  }, [networkStatus, startSync]);
};

export default useSyncService;
