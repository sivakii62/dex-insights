import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { apiPaths } from '../../core/api/api.config';
import { StoreListComponent } from './store-list.component';

/** Exposes StoreListComponent's protected store for the test only. */
type TestableStoreList = { readonly store: StoreListComponent['store'] };

const emptyPage = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, hasNext: false };

describe('StoreListComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StoreListComponent],
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

  it('cycles the offline-pumps sort through descending, ascending, then back to unsorted', () => {
    const fixture = TestBed.createComponent(StoreListComponent);
    fixture.detectChanges();
    const { store } = fixture.componentInstance as unknown as TestableStoreList;
    httpMock.expectOne((req) => req.url === apiPaths.stores()).flush(emptyPage);

    expect(store.offlinePumpsSortState()).toBe('none');

    store.toggleOfflinePumpsSort();
    fixture.detectChanges();
    expect(store.offlinePumpsSortState()).toBe('desc');
    httpMock.expectOne((req) => req.params.get('sortBy') === 'OFFLINE_PUMPS' && req.params.get('direction') === 'DESC')
      .flush(emptyPage);

    store.toggleOfflinePumpsSort();
    fixture.detectChanges();
    expect(store.offlinePumpsSortState()).toBe('asc');
    httpMock.expectOne((req) => req.params.get('sortBy') === 'OFFLINE_PUMPS' && req.params.get('direction') === 'ASC')
      .flush(emptyPage);

    store.toggleOfflinePumpsSort();
    fixture.detectChanges();
    expect(store.offlinePumpsSortState()).toBe('none');
    httpMock.expectOne((req) => req.params.get('sortBy') === 'STORE_ID' && req.params.get('direction') === 'ASC')
      .flush(emptyPage);
  });

  it('sends a trimmed brand as a query parameter, so a partial term like "7" can match "7-Eleven"', () => {
    const fixture = TestBed.createComponent(StoreListComponent);
    fixture.detectChanges();
    const { store } = fixture.componentInstance as unknown as TestableStoreList;
    httpMock.expectOne((req) => req.url === apiPaths.stores()).flush(emptyPage);

    store.setBrand('  7  ');
    fixture.detectChanges();

    const request = httpMock.expectOne((req) => req.url === apiPaths.stores());
    expect(request.request.params.get('brand')).toBe('7');
    request.flush(emptyPage);
  });

  it('surfaces a request failure as a readable error message', () => {
    const fixture = TestBed.createComponent(StoreListComponent);
    fixture.detectChanges();
    const { store } = fixture.componentInstance as unknown as TestableStoreList;

    httpMock.expectOne((req) => req.url === apiPaths.stores()).flush(
      { title: 'Service unavailable', detail: 'The store index is temporarily unavailable.' },
      { status: 503, statusText: 'Service Unavailable' },
    );
    fixture.detectChanges();

    expect(store.error()).toBe('The store index is temporarily unavailable.');
  });
});
