import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  numberAttribute,
  signal,
  untracked
} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {Flashcard} from './flashcard/flashcard';
import {MatIconButton} from '@angular/material/button';
import {FlashcardsStore} from '../../state/flashcards.store';

// Shows the flashcards of one collection one at a time, opened from a row of the collections table
@Component({
  selector: 'app-flashcards-page',
  imports: [MatIconModule, RouterLink, Flashcard, MatIconButton],
  providers: [FlashcardsStore],
  templateUrl: './flashcards-page.html',
  styleUrl: './flashcards-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(document:keydown.arrowleft)': 'goToPreviousFlashcard()',
    '(document:keydown.arrowright)': 'goToNextFlashcard()',
  },
})
export class FlashcardsPage {
  // Bound from the :collectionId route param (withComponentInputBinding in app.config.ts)
  readonly collectionId = input.required({transform: numberAttribute});
  protected flashcardStore = inject(FlashcardsStore);
  // Position of the shown card in the whole collection, not just in the loaded page
  protected position = signal(0);
  // Which way the cards slide: forward moves them to the left (next), backward to the right (previous)
  protected direction = signal<'forward' | 'backward'>('forward');

  constructor() {
    // The store loads nothing until it knows the collection, and starts over when the route param changes
    effect(() => {
      const collectionId = this.collectionId();
      untracked(() => {
        this.position.set(0);
        this.flashcardStore.setCollection(collectionId);
      });
    });
  }

  protected total = this.flashcardStore.totalElements;

  // Null while the page holding the card is still loading
  protected flashcardToRender = computed(() => {
    const cards = this.flashcardStore.flashcards();
    return cards[this.position() % this.flashcardStore.pageSize()] ?? null;
  });

  // Zero or one card, for the @for in the template: tracking by id swaps the element when the card changes,
  // which is what runs animate.leave on the old card and animate.enter on the new one
  protected visibleFlashcards = computed(() => {
    const card = this.flashcardToRender();
    return card ? [card] : [];
  });

  protected progress = computed(() => (this.total() ? ((this.position() + 1) / this.total()) * 100 : 0));

  // Both buttons wrap around, so there is nowhere to go only with fewer than two cards
  protected canMove = computed(() => this.total() > 1);

  protected goToPreviousFlashcard(): void {
    if (!this.canMove()) return;
    this.direction.set('backward');
    // from the first card, go round to the last one
    this.goTo(this.position() === 0 ? this.total() - 1 : this.position() - 1);
  }

  protected goToNextFlashcard(): void {
    if (!this.canMove()) return;
    this.direction.set('forward');
    // from the last card, go round to the first one
    this.goTo(this.position() + 1 >= this.total() ? 0 : this.position() + 1);
  }

  private goTo(position: number): void {
    this.position.set(position);
    const pageSize = this.flashcardStore.pageSize();
    const page = Math.floor(position / pageSize);
    if (page !== this.flashcardStore.pageIndex()) {
      this.flashcardStore.setPage(page, pageSize);
    }
  }
}
