import {Component, inject, input, model} from '@angular/core';
import {MatCard, MatCardContent} from '@angular/material/card';
import {CardTextColor, SettingsStore} from '../../../state/settings.store';

const TEXT_COLORS: Record<CardTextColor, string> = {
  dark: 'var(--fg-grey-900)',
  light: 'var(--fg-white)',
};

@Component({
  selector: 'app-flashcard',
  imports: [
    MatCard,
    MatCardContent
  ],
  templateUrl: './flashcard.html',
  styleUrl: './flashcard.scss',
})
export class Flashcard {
  public front = input<string>("");
  public back = input<string>("");
  // A model, so the settings preview can show a card already turned to its back
  public flipped = model<boolean>(false);
  // The colours chosen on the settings page; a null background keeps the default gradient
  protected readonly settings = inject(SettingsStore);
  protected readonly textColors = TEXT_COLORS;

  flipCard() {
    this.flipped.update(prevValue => !prevValue)
  }

}
