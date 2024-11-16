import { SerializedError } from "@reduxjs/toolkit";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";

export const baseUrl = 'http://localhost:3000';

export const getLogger: (tag: string) => (...args: any) => void =
    (tag: string) => (...args: any) => console.log(tag, ...args);

const log = getLogger('core');

export interface ResponseProps<T> {
    data: T,
}

export async function withLogs<T>(promise: Promise<ResponseProps<T>>, opName: string): Promise<T> {
    log(`${opName} - started`);
    try {
        const result = await promise;
        log(`${opName} - success`);
        return await Promise.resolve(result.data);
    } catch (error) {
        log(`${opName} - failed`, error);
        return await Promise.reject(error);
    }
}

export const requestConfig = {
    headers: {
        'Content-Type': 'application/json',
    }
}

export const getErrorMessage: (error: FetchBaseQueryError | SerializedError | undefined, baseErrorMessage: string) => string = (error, baseErrorMessage) => {
    if (!error) {
        return "";
    }

    let errorMessage = "";

    if ('status' in error) {
        errorMessage = 'error' in error ? error.error : error.data.message;
    } else {
        errorMessage = error.message ?? '';
    }

    if (errorMessage === '') {
        errorMessage = baseErrorMessage;
    }

    return errorMessage;
}

export interface WSProps {
    ws: WebSocket;
    sendLogoutMessage: () => void;
}

interface MessageData {
    type: string;
    payload: any;
    sender: string;
}

export const newWebSocket = (token: string, onMessage: (data: MessageData) => void) => {
    const ws = new WebSocket(`${baseUrl.replace('http', 'ws')}/ws`);

    ws.onopen = () => {
        log('WS onopen');
        ws.send(JSON.stringify({ type: 'authorization', payload: token, sender: 'client' }));
    };

    ws.onclose = (event) => {
        log('WS onclose', event);
        if (!event.wasClean) {
            log('WS onclose - connection closed unexpectedly');
        }
    };

    ws.onerror = (error) => {
        log('WS onerror', error);
    };

    ws.onmessage = (message) => {
        log('WS onmessage', message);
        try {
            const data = JSON.parse(message.data);
            onMessage(data);
        } catch (error) {
            log('WS onmessage - error', error);
        }

    };

    const sendLogoutMessage = () => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: 'logout', payload: '', sender: 'client' }));
        }
    }

    return { ws, sendLogoutMessage };
}