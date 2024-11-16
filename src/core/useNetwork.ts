import { useEffect, useState } from "react";
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

  useEffect(() => {
    let handler: PluginListenerHandle;

    registerNetworkListener();
    Network.getStatus().then(handlerNetworkStatus);

    let cancelled = false;

    return () => {
      cancelled = true;
      handler?.remove();
    };

    async function registerNetworkListener() {
      handler = await Network.addListener(
        "networkStatusChange",
        handlerNetworkStatus
      );
    }

    async function handlerNetworkStatus(status: ConnectionStatus) {
      if (cancelled) {
        return;
      }

      log("network status", status);
      setNetworkStatus({
        isConnected: status.connected,
        connectionType: status.connectionType,
      });
    }
  }, []);

  return { networkStatus };
};

export default useNetwork;
