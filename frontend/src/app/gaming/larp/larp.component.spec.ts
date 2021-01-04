import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LarpComponent } from './larp.component';

describe('LarpComponent', () => {
  let component: LarpComponent;
  let fixture: ComponentFixture<LarpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LarpComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LarpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
