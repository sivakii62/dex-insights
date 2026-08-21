import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'stores' },
  {
    path: 'stores',
    loadComponent: () => import('./features/store-list/store-list.component').then((m) => m.StoreListComponent),
    title: 'Stores · Dex Insights',
  },
  {
    path: 'stores/:storeId',
    loadComponent: () =>
      import('./features/store-detail/store-detail.component').then((m) => m.StoreDetailComponent),
    title: 'Store detail · Dex Insights',
  },
  {
    path: 'insights',
    loadComponent: () => import('./features/insights/insights.component').then((m) => m.InsightsComponent),
    title: 'Insights · Dex Insights',
  },
  {
    path: 'chat',
    loadComponent: () => import('./features/chat/chat.component').then((m) => m.ChatComponent),
    title: 'Chat · Dex Insights',
  },
  { path: '**', redirectTo: 'stores' },
];
