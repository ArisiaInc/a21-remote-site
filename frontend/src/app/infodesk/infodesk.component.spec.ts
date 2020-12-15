import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InfodeskComponent } from './infodesk.component';

describe('InfodeskComponent', () => {
  let component: InfodeskComponent;
  let fixture: ComponentFixture<InfodeskComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InfodeskComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InfodeskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
