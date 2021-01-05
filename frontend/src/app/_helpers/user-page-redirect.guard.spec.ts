import { TestBed } from '@angular/core/testing';

import { UserPageRedirectGuard } from './user-page-redirect.guard';

describe('UserPageRedirectGuard', () => {
  let guard: UserPageRedirectGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(UserPageRedirectGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
