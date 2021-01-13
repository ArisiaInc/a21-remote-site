import { TestBed } from '@angular/core/testing';

import { RibbonService } from './ribbon.service';

describe('RibbonService', () => {
  let service: RibbonService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RibbonService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
