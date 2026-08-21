import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

/** A dismissable/retryable error banner. Kept generic so every feature can reuse the same shape. */
@Component({
  selector: 'app-error-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="error-state" role="alert">
      <p>{{ message() }}</p>
      @if (retryable()) {
        <button type="button" class="btn btn--secondary" (click)="retry.emit()">Try again</button>
      }
    </div>
  `,
  styles: `
    .error-state {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-3);
      padding: var(--space-3) var(--space-4);
      background: var(--color-critical-bg);
      color: var(--color-critical-fg);
      border-radius: var(--radius-md);
    }
  `,
})
export class ErrorStateComponent {
  readonly message = input.required<string>();
  readonly retryable = input(true);
  readonly retry = output<void>();
}
