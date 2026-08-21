import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** A visually simple, screen-reader-friendly loading indicator used while data is in flight. */
@Component({
  selector: 'app-loading-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<p class="loading-state" role="status">{{ label() }}</p>`,
  styles: `
    .loading-state {
      color: var(--color-text-muted);
      padding: var(--space-4) 0;
    }
  `,
})
export class LoadingStateComponent {
  readonly label = input('Loading…');
}
