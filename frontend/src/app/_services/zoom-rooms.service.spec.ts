import { TestBed } from '@angular/core/testing';

import { ZoomRoomsService } from './zoom-rooms.service';

describe('ZoomRoomsService', () => {
  let service: ZoomRoomsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ZoomRoomsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
