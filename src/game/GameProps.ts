export interface GameProps {
    _id?: string;
    title: string;
    release_date: string;
    rental_price: number;
    rating: number;
    category: string;
    date?: string;
    version?: number;
}

export const GameCategory: string[] = [
    'Action', 'Adventure', 'Fighting', 'Misc',
    'Platform', 'Puzzle', 'Racing', 'Role-Playing',
    'Shooter', 'Simulation', 'Sports', 'Strategy'
];