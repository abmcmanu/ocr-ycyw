import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdvisorConsoleComponent } from './advisor-console.component';

describe('AdvisorConsoleComponent', () => {
  let component: AdvisorConsoleComponent;
  let fixture: ComponentFixture<AdvisorConsoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdvisorConsoleComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdvisorConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
