import { TestBed } from '@angular/core/testing';

import { StarsService } from './stars.service';

describe('StarsService', () => {
  let service: StarsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StarsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
