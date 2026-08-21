import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';
import { ChatStore } from './chat.store';

const EXAMPLE_QUESTIONS = [
  'Which stores have the highest offline pumps and what incidents are associated with them?',
  'Summarize store 10001 health and recent activity',
  'Any stores with low tank levels that look like runout risk?',
];

/** Grounded Q&A view. Submits through ChatStore and renders the answer alongside its citations. */
@Component({
  selector: 'app-chat',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, LoadingStateComponent, ErrorStateComponent],
  providers: [ChatStore],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss',
})
export class ChatComponent {
  protected readonly store = inject(ChatStore);

  protected readonly exampleQuestions = EXAMPLE_QUESTIONS;

  protected readonly form = new FormGroup({
    question: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }),
    storeId: new FormControl('', { nonNullable: true }),
  });

  protected askExample(question: string): void {
    this.form.controls.question.setValue(question);
    this.submit();
  }

  protected submit(): void {
    if (this.form.invalid || this.store.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    const { question, storeId } = this.form.getRawValue();
    this.store.ask({ question: question.trim(), storeId: storeId.trim() || undefined });
  }
}
