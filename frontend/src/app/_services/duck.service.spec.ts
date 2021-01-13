import { TestBed } from '@angular/core/testing';

import { DuckService } from './duck.service';

describe('DuckService', () => {
  let service: DuckService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DuckService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
