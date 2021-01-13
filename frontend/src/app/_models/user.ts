export interface User {
  id: string;
  name: string;
  badgeNumber: string;
  zoomHost: boolean;
  ducks?: number[];
  self?: boolean;
  ribbons?: number[];
}
