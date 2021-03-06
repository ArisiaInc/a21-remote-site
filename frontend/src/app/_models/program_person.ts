import { ProgramItem } from './program_item'

export interface ProgramPerson {
    id: string;
    name: string[] | string;
    tags?: string[];
    links?: object;
    bio?: string;
    prog?: string[];
    items?: {[_:string]: {[_:string]: ProgramItem[]}};
}
