import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RibbonModalComponent } from './ribbon-modal.component';

describe('RibbonModalComponent', () => {
  let component: RibbonModalComponent;
  let fixture: ComponentFixture<RibbonModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RibbonModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RibbonModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
