import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpecialComponent } from './special.component';

describe('SpecialComponent', () => {
  let component: SpecialComponent;
  let fixture: ComponentFixture<SpecialComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SpecialComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SpecialComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
