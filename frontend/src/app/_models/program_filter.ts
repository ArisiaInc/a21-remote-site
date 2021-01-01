export interface ProgramFilter {
  loc?: string[];
  date?: {start: Date, end: Date}[];
  tags?: string[];
  id?: string[];
}
