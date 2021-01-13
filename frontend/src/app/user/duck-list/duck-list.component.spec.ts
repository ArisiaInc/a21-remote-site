import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DuckListComponent } from './duck-list.component';

describe('DuckListComponent', () => {
  let component: DuckListComponent;
  let fixture: ComponentFixture<DuckListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DuckListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DuckListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
