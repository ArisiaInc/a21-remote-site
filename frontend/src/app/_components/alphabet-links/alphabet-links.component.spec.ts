import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlphabetLinksComponent } from './alphabet-links.component';

describe('AlphabetLinksComponent', () => {
  let component: AlphabetLinksComponent;
  let fixture: ComponentFixture<AlphabetLinksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AlphabetLinksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AlphabetLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
