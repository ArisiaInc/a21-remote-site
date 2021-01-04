export type PreferredLink = "web" | "etsy" | "insta" | "facebook" | "youtube" | "other";

export interface Creator {
    id: string;
    name: string;
    links?: {
        web?: string;
        etsy?: string;
        youtube?: string;
        insta?: string;
        facebook?: string;
        other?: string;
        preferred: PreferredLink;
    }
    summary: string;
    description?: string;
    images: CreatorImage[];
    order: number;
}

// TODO do we need priority/order for images?

export interface CreatorImage {
    creator_id: string;
    url: string;
    alt: string;
    title: string;
    description?: string;
    image_id: string;
}