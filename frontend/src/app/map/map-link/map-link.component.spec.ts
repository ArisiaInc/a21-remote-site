import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapLinkComponent } from './map-link.component';

describe('MapLinkComponent', () => {
  let component: MapLinkComponent;
  let fixture: ComponentFixture<MapLinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MapLinkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MapLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
