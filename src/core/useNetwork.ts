import { useCallback, useEffect, useState } from "react";
import { PluginListenerHandle } from "@capacitor/core";
import { ConnectionStatus, Network } from "@capacitor/network";
import { getLogger } from ".";

export interface NetworkStatus {
  isConnected: boolean;
  connectionType: string;
}

const initialStatus: NetworkStatus = {
  isConnected: true,
  connectionType: "unknown",
};

const log = getLogger("useNetwork");

const useNetwork = () => {
  const [networkStatus, setNetworkStatus] =
    useState<NetworkStatus>(initialStatus);

  const handlerNetworkStatus = useCallback((status: ConnectionStatus) => {
    log("network status", status);
    setNetworkStatus((prev) => {
      if (
        prev.isConnected !== status.connected ||
        prev.connectionType !== status.connectionType
      ) {
        return {
          isConnected: status.connected,
          connectionType: status.connectionType,
        };
      }

      return prev;
    });
  }, []);

  useEffect(() => {
    let cancelled = false;
    let handler: PluginListenerHandle;

    async function registerNetworkListener() {
      try {
        const status = await Network.getStatus();
        if (!cancelled) {
          handlerNetworkStatus(status);
        }

        handler = await Network.addListener(
          "networkStatusChange",
          handlerNetworkStatus
        );
      } catch (error) {
        log("registerNetworkListener", error);
      }
    }

    registerNetworkListener();

    return () => {
      cancelled = true;
      handler?.remove();
    };
  }, [handlerNetworkStatus]);

  return { networkStatus };
};

export default useNetwork;
