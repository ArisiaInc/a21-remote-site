export interface Duck {
    id: number;
    imageUrl: string;
    alt: string;
    link: string;
    hint: string;
    requesting_url: string;
}

export interface DuckState {
    duck: Duck;
    hidden: boolean;
}
