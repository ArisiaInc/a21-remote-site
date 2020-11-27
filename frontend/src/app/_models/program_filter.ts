export class ProgramFilter {
    loc?: string[];
    date?: string[];
    tags?: string[];
    id?: string[];
}

export class MungedProgramFilter {
    tags?: (_:string[]) => boolean;
    loc?: (_:string[]) => boolean;
    date?: (_:string) => boolean;
    id?: (_:string) => boolean;
}