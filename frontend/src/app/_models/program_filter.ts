/** Events match a DateRange if the start of the even is during the
 * interval. If inclusive is true, then an event that has started
 * before the DateRange's start, but is still going will match. */
export interface DateRange {
  start: Date;
  end?: Date;
  inclusive?: boolean;
}

/** Events only filter if they match all 4 filters. Empty filters
 * match. */
export interface ProgramFilter {
  /** Matches if any of the rooms match. */
  loc?: string[];
  /** Matches if any of the Date Ranges match. */
  date?: DateRange[];
  /** filter by tag. This is a list of lists. The inner lists of tags
   * are ORed together and the outer lists of lists are ANDed
   * together. */
  tags?: string[][];
  /** Matches if any of the ids match the id of the event. */
  id?: string[];
}
