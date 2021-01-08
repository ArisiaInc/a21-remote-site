import { TestBed } from '@angular/core/testing';

import { XByNameService } from './x-by-name.service';

describe('XByNameService', () => {
  let service: XByNameService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(XByNameService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
