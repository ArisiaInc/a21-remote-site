import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileDuckComponent } from './profile-duck.component';

describe('ProfileDuckComponent', () => {
  let component: ProfileDuckComponent;
  let fixture: ComponentFixture<ProfileDuckComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProfileDuckComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileDuckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
