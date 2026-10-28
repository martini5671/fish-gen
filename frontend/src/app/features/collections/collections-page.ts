import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { CollectionDto } from '../../api/models/collection-dto';
import { CollectionSort, CollectionsStore } from './collections.store';

const LANGUAGE_LABELS: Record<NonNullable<CollectionDto['targetLanguage']>, string> = {
  POLISH: 'Polish',
  ENGLISH: 'English',
};

const STATUS_LABELS: Record<CollectionDto['generationStatus'], string> = {
  PENDING: 'Generating',
  COMPLETED: 'Ready',
  FAILED: 'Failed',
};

@Component({
  selector: 'app-collections-page',
  imports: [
    DatePipe,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
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

  protected readonly columns = ['name', 'targetLanguage', 'generationStatus', 'createdAt', 'actions'];
  protected readonly pageSizeOptions = [10, 25, 50];

  protected readonly sortOptions: { value: CollectionSort; label: string }[] = [
    { value: 'NEWEST', label: 'Newest first' },
    { value: 'OLDEST', label: 'Oldest first' },
    { value: 'NAME_ASC', label: 'Name A–Z' },
  ];

  // mat-table rows are typed as any, so the template reads labels through these typed helpers
  protected languageLabel(collection: CollectionDto): string {
    return collection.targetLanguage ? LANGUAGE_LABELS[collection.targetLanguage] : '–';
  }

  protected statusLabel(collection: CollectionDto): string {
    return STATUS_LABELS[collection.generationStatus];
  }

  protected statusClass(collection: CollectionDto): string {
    return `status status-${collection.generationStatus.toLowerCase()}`;
  }

  protected onPage(event: PageEvent): void {
    this.store.setPage(event.pageIndex, event.pageSize);
  }
}
