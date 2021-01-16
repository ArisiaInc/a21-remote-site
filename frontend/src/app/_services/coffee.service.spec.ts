import { TestBed } from '@angular/core/testing';

import { CoffeeService } from './coffee.service';

describe('CoffeeService', () => {
  let service: CoffeeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CoffeeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
