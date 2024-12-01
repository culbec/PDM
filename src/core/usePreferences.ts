import { Preferences } from "@capacitor/preferences";
import { useCallback, useRef } from "react";
import { getLogger } from ".";
import { debounce } from "lodash";

const log = getLogger("usePreferences");
const TIMEOUT_MS = 5000;
const MAX_RETRIES = 2;

export function usePreferences() {
  const cache = useRef<Map<string, string>>(new Map());
  const pendingRequests = useRef<Map<string, Promise<any>>>(new Map());
  const retryCount = useRef<Map<string, number>>(new Map());

  const getPreference = useCallback(async (key: string) => {
    // Return cached value if available
    if (cache.current.has(key)) {
      return cache.current.get(key);
    }

    // If there's already a pending request for this key, return its promise
    if (pendingRequests.current.has(key)) {
      return pendingRequests.current.get(key);
    }

    try {
      const currentRetries = retryCount.current.get(key) || 0;
      if (currentRetries >= MAX_RETRIES) {
        log(`Max retries reached for ${key}, returning empty result`);
        return null;
      }

      const startTime = performance.now();
      retryCount.current.set(key, currentRetries + 1);

      const fetchPromise = (async () => {
        try {
          const timeoutPromise = new Promise<{ value?: string }>((_, reject) => {
            setTimeout(() => {
              pendingRequests.current.delete(key);
              reject(new Error(`Preferences.get(${key}) timed out`));
            }, TIMEOUT_MS);
          });

          const result = await Promise.race([
            Preferences.get({ key }),
            timeoutPromise,
          ]);

          if (result?.value) {
            cache.current.set(key, result.value);
            retryCount.current.delete(key);
          }
          
          pendingRequests.current.delete(key);
          return result?.value || null;
        } catch (error) {
          pendingRequests.current.delete(key);
          throw error;
        }
      })();

      pendingRequests.current.set(key, fetchPromise);
      const value = await fetchPromise;
      log(`Preferences.get(${key}) took ${performance.now() - startTime}ms`);
      return value;
    } catch (error) {
      log(`Preferences.get(${key}) failed:`, error);
      pendingRequests.current.delete(key);
      return null;
    }
  }, []);

  const debouncedSetPreference = useCallback(
    debounce(async (key: string, value: string) => {
      try {
        await Preferences.set({ key, value });
        cache.current.set(key, value);
      } catch (error) {
        log(`Preferences.set(${key}) failed:`, error);
        cache.current.delete(key);
      }
    }, 300),
    []
  );

  const setPreference = useCallback(
    (key: string, value: string) => {
      cache.current.set(key, value);
      return debouncedSetPreference(key, value);
    },
    [debouncedSetPreference]
  );

  return { getPreference, setPreference };
}