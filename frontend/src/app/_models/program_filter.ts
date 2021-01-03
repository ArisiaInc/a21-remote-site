export interface DateRange {
  start: Date;
  end: Date;
  inclusive?: boolean;
}

export interface ProgramFilter {
  loc?: string[];
  date?: DateRange[];
  tags?: string[];
  id?: string[];
}
