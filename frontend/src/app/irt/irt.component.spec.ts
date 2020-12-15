import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IrtComponent } from './irt.component';

describe('IrtComponent', () => {
  let component: IrtComponent;
  let fixture: ComponentFixture<IrtComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IrtComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IrtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
