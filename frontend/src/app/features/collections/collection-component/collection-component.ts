import {Component, inject, input, output, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {MatButton, MatFabButton, MatMiniFabButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-collection-component',
  imports: [
    DatePipe,
    MatButton,
    MatFabButton,
    MatIcon,
    MatMiniFabButton
  ],
  templateUrl: './collection-component.html',
  styleUrl: './collection-component.scss',
})
export class CollectionComponent {
  private router = inject(Router);
  readonly collectionName = input<string>('');
  readonly collectionId = input<number>(0);
  readonly creationDate = input<string>('');
  /** Emits when the card is clicked (or activated with Enter/Space). */
  readonly selected = output<number>();

  protected openCollection() {
    void this.router.navigate(['/collections', this.collectionId()])
  }
}
