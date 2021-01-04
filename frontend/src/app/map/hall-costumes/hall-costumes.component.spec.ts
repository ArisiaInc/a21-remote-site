import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HallCostumesComponent } from './hall-costumes.component';

describe('HallCostumesComponent', () => {
  let component: HallCostumesComponent;
  let fixture: ComponentFixture<HallCostumesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HallCostumesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HallCostumesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
