import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ChatApiService } from '../../core/api/chat-api.service';
import { ApiError } from '../../core/api/api-error';
import type { ChatResponse } from '../../core/models/chat.model';
import { ErrorStateComponent } from '../../shared/ui/error-state.component';
import { LoadingStateComponent } from '../../shared/ui/loading-state.component';

const EXAMPLE_QUESTIONS = [
  'Which stores have the highest offline pumps and what incidents are associated with them?',
  'Summarize store 10001 health and recent activity',
  'Any stores with low tank levels that look like runout risk?',
];

/** Grounded Q&A view. Submits to POST /v1/chat and renders the answer alongside its citations. */
@Component({
  selector: 'app-chat',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, LoadingStateComponent, ErrorStateComponent],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss',
})
export class ChatComponent {
  private readonly chatApi = inject(ChatApiService);

  protected readonly exampleQuestions = EXAMPLE_QUESTIONS;

  protected readonly form = new FormGroup({
    question: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }),
    storeId: new FormControl('', { nonNullable: true }),
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly response = signal<ChatResponse | null>(null);

  protected askExample(question: string): void {
    this.form.controls.question.setValue(question);
    this.submit();
  }

  protected submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    const { question, storeId } = this.form.getRawValue();
    this.loading.set(true);
    this.errorMessage.set(null);

    this.chatApi.ask({ question: question.trim(), storeId: storeId.trim() || undefined }).subscribe({
      next: (result) => {
        this.response.set(result);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(error instanceof ApiError ? error.message : 'Something went wrong answering that.');
        this.loading.set(false);
      },
    });
  }
}
