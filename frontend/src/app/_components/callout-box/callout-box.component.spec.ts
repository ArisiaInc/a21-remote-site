import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalloutBoxComponent } from './callout-box.component';

describe('CalloutBoxComponent', () => {
  let component: CalloutBoxComponent;
  let fixture: ComponentFixture<CalloutBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CalloutBoxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CalloutBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
