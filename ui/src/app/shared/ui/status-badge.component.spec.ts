import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { StatusBadgeComponent } from './status-badge.component';

describe('StatusBadgeComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideZonelessChangeDetection()] });
  });

  it('infers a critical tone for OFFLINE, OPEN and HIGH', () => {
    for (const label of ['OFFLINE', 'OPEN', 'HIGH']) {
      const fixture = TestBed.createComponent(StatusBadgeComponent);
      fixture.componentRef.setInput('label', label);
      fixture.detectChanges();
      const badge = (fixture.nativeElement as HTMLElement).querySelector('.badge');
      expect(badge?.classList.contains('badge--critical')).toBe(true);
    }
  });

  it('infers a positive tone for ONLINE and lets an explicit tone override inference', () => {
    const fixture = TestBed.createComponent(StatusBadgeComponent);
    fixture.componentRef.setInput('label', 'ONLINE');
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('.badge')?.classList.contains('badge--positive')).toBe(true);

    fixture.componentRef.setInput('tone', 'warning');
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('.badge')?.classList.contains('badge--warning')).toBe(true);
  });
});
