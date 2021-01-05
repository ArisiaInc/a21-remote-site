import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoorDragonComponent } from './door-dragon.component';

describe('DoorDragonComponent', () => {
  let component: DoorDragonComponent;
  let fixture: ComponentFixture<DoorDragonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DoorDragonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DoorDragonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
