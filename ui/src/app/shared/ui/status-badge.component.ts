import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Small colored label for a store status or incident severity. Color is data, not decoration. */
@Component({
  selector: 'app-status-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class]="'badge--' + resolvedTone()">{{ label() }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 0.15rem 0.6rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.02em;
      text-transform: uppercase;
      line-height: 1.6;
      white-space: nowrap;
    }
    .badge--positive {
      background: var(--color-positive-bg);
      color: var(--color-positive-fg);
    }
    .badge--warning {
      background: var(--color-warning-bg);
      color: var(--color-warning-fg);
    }
    .badge--critical {
      background: var(--color-critical-bg);
      color: var(--color-critical-fg);
    }
    .badge--neutral {
      background: var(--color-neutral-bg);
      color: var(--color-neutral-fg);
    }
  `,
})
export class StatusBadgeComponent {
  readonly label = input.required<string>();

  /** Explicit tone always wins; otherwise it is inferred from common status/severity vocab. */
  readonly tone = input<'positive' | 'warning' | 'critical' | 'neutral' | undefined>(undefined);

  protected readonly resolvedTone = computed(() => this.tone() ?? this.inferTone(this.label()));

  private inferTone(label: string): 'positive' | 'warning' | 'critical' | 'neutral' {
    const value = label.toUpperCase();
    if (value === 'ONLINE' || value === 'RESOLVED' || value === 'LOW') return 'positive';
    if (value === 'DEGRADED' || value === 'ACKNOWLEDGED' || value === 'MEDIUM') return 'warning';
    if (value === 'OFFLINE' || value === 'OPEN' || value === 'HIGH') return 'critical';
    return 'neutral';
  }
}
