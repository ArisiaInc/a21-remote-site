import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RibbonTableComponent } from './ribbon-table.component';

describe('RibbonTableComponent', () => {
  let component: RibbonTableComponent;
  let fixture: ComponentFixture<RibbonTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RibbonTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RibbonTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
