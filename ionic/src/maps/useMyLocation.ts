import { Geolocation, Position } from "@capacitor/geolocation";
import { useEffect, useState } from "react";
import { getLogger } from "../core";

const log = getLogger("useMaps");

interface MyLocation {
  position?: Position;
  error?: Error;
  loading: boolean;
}

export function useMyLocation() {
  const [state, setState] = useState<MyLocation>({ loading: true });

  useEffect(() => {
    let mounted = true;
    let callbackId: string | null = null;

    function updateMyPosition(
      source: string,
      position?: Position,
      error?: any
    ) {
      log(`updateMyPosition from ${source}`, position, error);
      if (mounted) {
        setState({ position, error, loading: false });
      }
    }

    fetchPosition();

    return () => {
      mounted = false;
      if (callbackId) {
        Geolocation.clearWatch({ id: callbackId });
      }
    };

    async function fetchPosition() {
      try {
        const postion = await Geolocation.getCurrentPosition();
        updateMyPosition("current", postion);
      } catch (error) {
        updateMyPosition("current", undefined, error);
      }

      callbackId = await Geolocation.watchPosition({}, (position, error) => {
        if (mounted) {
          if (position) {
            updateMyPosition("watch", position, error);
          } else {
            updateMyPosition("watch", undefined, error);
          }
        }
      });
    }
  }, []);

  return state;
}
