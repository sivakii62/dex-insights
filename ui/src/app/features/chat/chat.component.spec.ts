import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { apiPaths } from '../../core/api/api.config';
import { ChatComponent } from './chat.component';

/** Exposes ChatComponent's protected surface for the test only. */
type TestableChat = {
  readonly form: ChatComponent['form'];
  readonly exampleQuestions: ChatComponent['exampleQuestions'];
  readonly store: ChatComponent['store'];
  submit(): void;
  askExample(question: string): void;
};

describe('ChatComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('does not submit a blank question', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as TestableChat;

    component.submit();

    httpMock.expectNone(apiPaths.chat());
    expect(component.form.controls.question.touched).toBe(true);
  });

  it('posts the question and store id, then renders the answer and citations', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as TestableChat;

    component.form.setValue({ question: 'Summarize store 10001', storeId: '10001' });
    component.submit();

    const request = httpMock.expectOne(apiPaths.chat());
    expect(request.request.body).toEqual({ question: 'Summarize store 10001', storeId: '10001' });

    request.flush({
      answer: 'Store 10001 is online with all pumps active.',
      citations: [
        { type: 'STORE', recordId: '10001', storeId: '10001', timestamp: null, snippet: 'Store 10001...', relevance: 1 },
      ],
      retrievedContextSummary: 'Retrieved 1 record.',
    });
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Store 10001 is online with all pumps active.');
    expect(text).toContain('Retrieved 1 record.');
  });

  it('fills the form and submits when an example question is chosen', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as TestableChat;

    const [firstExample] = component.exampleQuestions;
    component.askExample(firstExample);

    expect(component.form.controls.question.value).toBe(firstExample);
    httpMock.expectOne(apiPaths.chat()).flush({ answer: '', citations: [], retrievedContextSummary: '' });
  });

  it('will not submit again while a request is already in flight', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as TestableChat;

    component.form.setValue({ question: 'Summarize store 10001', storeId: '' });
    component.submit();
    expect(component.store.loading()).toBe(true);

    component.submit();
    httpMock.expectOne(apiPaths.chat()).flush({ answer: '', citations: [], retrievedContextSummary: '' });
  });
});
