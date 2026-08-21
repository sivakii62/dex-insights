import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { StoreApiService } from './store-api.service';
import { apiPaths } from './api.config';

describe('StoreApiService', () => {
  let service: StoreApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(StoreApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('sends only the query parameters that are actually set', () => {
    service.list({ sortBy: 'OFFLINE_PUMPS', direction: 'DESC', page: 0, size: 10 }).subscribe();

    const request = httpMock.expectOne(
      (req) => req.url === apiPaths.stores() && req.params.get('sortBy') === 'OFFLINE_PUMPS',
    );
    expect(request.request.params.has('brand')).toBe(false);
    expect(request.request.params.has('status')).toBe(false);
    expect(request.request.params.get('direction')).toBe('DESC');
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, hasNext: false });
  });

  it('requests a single store by id', () => {
    service.getById('10001').subscribe();

    const request = httpMock.expectOne(apiPaths.store('10001'));
    expect(request.request.method).toBe('GET');
    request.flush({ storeId: '10001' });
  });
});
