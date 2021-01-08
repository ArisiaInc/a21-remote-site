import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomLinkOtherComponent } from './room-link-other.component';

describe('RoomLinkOtherComponent', () => {
  let component: RoomLinkOtherComponent;
  let fixture: ComponentFixture<RoomLinkOtherComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RoomLinkOtherComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RoomLinkOtherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
