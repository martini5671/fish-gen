import {DatePipe} from '@angular/common';
import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  Injector,
  signal,
  viewChild,
} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatDialog} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatTableModule} from '@angular/material/table';
import {Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, exhaustMap, map, Subject, timer} from 'rxjs';
import {CollectionDto} from '../../api/models/collection-dto';
import {CollectionSort, CollectionsStore} from '../../state/collections.store';
import {TokenUsageStore} from '../../state/token-usage.store';
import {capitalize} from '../../shared/capitalize';
import {ConfirmDialog, ConfirmDialogData} from '../../shared/confirm-dialog/confirm-dialog';
import {CreateCollectionDialog} from './create-collection-dialog/create-collection-dialog';

// Same limit as the backend's UpdateCollectionNameDto
const NAME_MAX_LENGTH = 255;

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
  private readonly router = inject(Router);

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

  // One row is renamed at a time; its name is edited in place in the table
  protected readonly editingId = signal<number | null>(null);
  protected readonly draftName = signal('');
  protected readonly draftError = computed(() => {
    const name = this.draftName().trim();
    if (!name) {
      return 'The name can not be empty';
    }
    return name.length > NAME_MAX_LENGTH ? `At most ${NAME_MAX_LENGTH} characters` : null;
  });
  private readonly nameInput = viewChild<ElementRef<HTMLInputElement>>('nameInput');
  private readonly injector = inject(Injector);

  protected startEdit(collection: CollectionDto): void {
    this.draftName.set(collection.name);
    this.editingId.set(collection.id);
    // the input only exists after the next render
    afterNextRender(() => this.nameInput()?.nativeElement.select(), {injector: this.injector});
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
  }

  protected saveName(collection: CollectionDto): void {
    if (this.draftError()) {
      return;
    }
    const name = this.draftName().trim();
    this.editingId.set(null);
    if (name !== collection.name) {
      this.store.renameCollection({id: collection.id, name});
    }
  }

  protected confirmDelete(collection: CollectionDto): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        width: '440px',
        maxWidth: 'calc(100vw - 32px)',
        data: {
          title: 'Delete collection?',
          message: `"${collection.name}" and all of its flashcards will be deleted. This can't be undone.`,
          confirmLabel: 'Delete',
          destructive: true,
        },
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed) {
          this.store.deleteCollection(collection.id);
        }
      });
  }

  protected onSearch(name: string): void {
    this.search$.next(name);
  }

  // keeps a row's <tr> when its collection object is replaced, so a status update doesn't recreate it
  protected readonly trackById = (_: number, collection: CollectionDto): number => collection.id;

  // mat-table rows are typed as any, so the template reads labels through these typed helper
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

  protected openCollection(collectionId: number): void {
    void this.router.navigate(['/collections', collectionId]);
  }
}
