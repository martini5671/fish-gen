import { ChangeDetectionStrategy, Component, input, numberAttribute } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

// Placeholder for the flashcards of one collection, opened from a row of the collections table
@Component({
  selector: 'app-flashcards-page',
  imports: [MatIconModule, RouterLink],
  templateUrl: './flashcards-page.html',
  styleUrl: './flashcards-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FlashcardsPage {
  // Bound from the :collectionId route param (withComponentInputBinding in app.config.ts)
  readonly collectionId = input.required({ transform: numberAttribute });
}
