import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { ColorPickerDirective } from 'ngx-color-picker';
import { Flashcard } from '../flashcards/flashcard/flashcard';
import { CardTextColor, SettingsStore } from '../../state/settings.store';

type Side = 'front' | 'back';

type Face = {
  key: Side;
  label: string;
  background: () => string | null;
  text: () => CardTextColor;
  setBackground: (color: string | null) => void;
  setText: (color: CardTextColor) => void;
  // Shown in the swatch while the default gradient is in use, and where the picker starts from
  defaultGradient: string;
  pickerStart: string;
};

// Lets the user pick the background and text colour of both flashcard faces, one side at a time, with a live preview.
// Every change is saved straight away by the SettingsStore
@Component({
  selector: 'app-settings-page',
  imports: [MatButtonModule, MatButtonToggleModule, MatIconModule, ColorPickerDirective, Flashcard],
  templateUrl: './settings-page.html',
  styleUrl: './settings-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsPage {
  protected readonly store = inject(SettingsStore);

  protected readonly presetColors = [
    '#ffffff', '#0f172a', '#2563eb', '#06b6d4', '#8b5cf6', '#ff1493', '#ffd700', '#22c55e',
  ];

  protected readonly faces: Face[] = [
    {
      key: 'front',
      label: 'Front',
      background: this.store.frontBackground,
      text: this.store.frontText,
      setBackground: (color) => this.store.setFrontBackground(color),
      setText: (color) => this.store.setFrontText(color),
      defaultGradient: 'var(--fg-gradient-card-front)',
      pickerStart: '#ff1493',
    },
    {
      key: 'back',
      label: 'Back',
      background: this.store.backBackground,
      text: this.store.backText,
      setBackground: (color) => this.store.setBackBackground(color),
      setText: (color) => this.store.setBackText(color),
      defaultGradient: 'var(--fg-gradient-card-back)',
      pickerStart: '#2563eb',
    },
  ];

  // The side being edited is the one the preview card shows, so flipping the card switches it
  protected readonly editing = signal<Side>('front');
  protected readonly face = computed(() => this.faces.find((face) => face.key === this.editing())!);
}
