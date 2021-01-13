import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RibbonListComponent } from './ribbon-list.component';

describe('RibbonListComponent', () => {
  let component: RibbonListComponent;
  let fixture: ComponentFixture<RibbonListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RibbonListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RibbonListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
