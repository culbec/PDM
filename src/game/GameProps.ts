export interface GameProps {
    _id?: string;
    title: string;
    release_date: string;
    rental_price: number;
    isRented: boolean;
    date?: string;
    version?: number;
}