export const baseUrl = 'http://localhost:8080';

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