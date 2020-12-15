export interface ProgramFilter {
    loc?: string[];
    date?: string[];
    tags?: string[];
    id?: string[];
}

export interface MungedProgramFilter {
    tags?: (_:string[]) => boolean;
    loc?: (_:string[]) => boolean;
    date?: (_:string) => boolean;
    id?: (_:string) => boolean;
}
