export interface Performance {
  sessionId: string;
  streamId: string
  platform: ('twitchChannel'|'twitchVideo'|'youtubeVideo'|'vimeoVideo');
  liveQA: boolean;
}
