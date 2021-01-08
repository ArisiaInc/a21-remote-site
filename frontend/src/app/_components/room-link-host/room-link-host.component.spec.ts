import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomLinkHostComponent } from './room-link-host.component';

describe('RoomLinkHostComponent', () => {
  let component: RoomLinkHostComponent;
  let fixture: ComponentFixture<RoomLinkHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RoomLinkHostComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RoomLinkHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
