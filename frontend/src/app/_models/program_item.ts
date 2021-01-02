export interface ProgramItem {
    id: string;
    title: string;
    tags: string[];
    date: string;
    time: string;
    timestamp: string;
    mins: string;
    loc: string[];
    people: {id: string, name: string}[];
    desc: string;
}
