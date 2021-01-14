export interface Ribbon {
    id: number;
    ribbonText: string;
    colorFg?: string;
    colorBg?: string;
    gradient?: string;
    imageFg?: string;
    imageBg?: string;
    secret: string;
    selfService: boolean;
}
