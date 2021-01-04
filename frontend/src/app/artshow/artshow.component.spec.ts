import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArtshowComponent } from './artshow.component';

describe('ArtshowComponent', () => {
  let component: ArtshowComponent;
  let fixture: ComponentFixture<ArtshowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ArtshowComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArtshowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
