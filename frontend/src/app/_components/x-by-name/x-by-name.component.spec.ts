import { ComponentFixture, TestBed } from '@angular/core/testing';

import { XByNameComponent } from './x-by-name.component';

describe('XByNameComponent', () => {
  let component: XByNameComponent;
  let fixture: ComponentFixture<XByNameComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ XByNameComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(XByNameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
