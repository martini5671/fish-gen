import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatDialog} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatTableModule} from '@angular/material/table';
import {debounceTime, distinctUntilChanged, exhaustMap, map, Subject, timer} from 'rxjs';
import {CollectionDto} from '../../api/models/collection-dto';
import {CollectionSort, CollectionsStore} from '../../state/collections.store';
import {TokenUsageStore} from '../../state/token-usage.store';
import {capitalize} from '../../shared/capitalize';
import {CreateCollectionDialog} from './create-collection-dialog/create-collection-dialog';

@Component({
  selector: 'app-collections-page',
  imports: [
    DatePipe,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
  ],
  providers: [CollectionsStore],
  templateUrl: './collections-page.html',
  styleUrl: './collections-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CollectionsPage {
  protected readonly store = inject(CollectionsStore);
  private readonly tokenUsageStore = inject(TokenUsageStore);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['name', 'targetLanguage', 'generationStatus', 'createdAt', 'actions'];
  protected readonly pageSizeOptions = [10, 25, 50];

  protected readonly sortOptions: { value: CollectionSort; label: string }[] = [
    {value: 'NEWEST', label: 'Newest first'},
    {value: 'OLDEST', label: 'Oldest first'},
    {value: 'NAME_ASC', label: 'Name A–Z'},
  ];

  private readonly search$ = new Subject<string>();

  constructor() {
    this.search$
      .pipe(
        debounceTime(300),
        map((name) => name.trim()),
        distinctUntilChanged(),
        takeUntilDestroyed(),
      )
      .subscribe((name) => {
        if (name.length ===0 || name.length >=3) {
          this.store.setName(name);
        }
      });
  }

  protected openCreateDialog(): void {
    this.dialog
      .open<CreateCollectionDialog, void, boolean>(CreateCollectionDialog, {
        width: '640px',
        maxWidth: 'calc(100vw - 32px)',
        position: { top: '88px' },
      })
      .afterClosed()
      .subscribe((created) => {
        if (created) {
          // The new collection shows up as PENDING, and generating it spent AI credits
          this.store.reload();
          this.tokenUsageStore.loadUsage();
        }
      });
  }

  protected onSearch(name: string): void {
    this.search$.next(name);
  }

  // mat-table rows are typed as any, so the template reads labels through these typed helpers
  protected languageLabel(collection: CollectionDto): string {
    return collection.targetLanguage ? capitalize(collection.targetLanguage) : '–';
  }

  protected statusLabel(collection: CollectionDto): string {
    return capitalize(collection.generationStatus);
  }

  protected statusClass(collection: CollectionDto): string {
    return `status status-${collection.generationStatus.toLowerCase()}`;
  }

  protected onPage(event: PageEvent): void {
    this.store.setPage(event.pageIndex, event.pageSize);
  }

}
