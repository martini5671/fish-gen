import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { form, FormField, maxLength, minLength, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ToastrService } from 'ngx-toastr';
import { firstValueFrom } from 'rxjs';
import { CreateFlashcardsDto } from '../../../api/models/create-flashcards-dto';
import { FlashcardsService } from '../../../api/services/flashcards.service';
import { capitalize } from '../../../shared/capitalize';

type TargetLanguage = CreateFlashcardsDto['targetLanguage'];

// Same limits as the backend's CreateFlashcardsDto validation
const TEXT_MIN_LENGTH = 10;
const TEXT_MAX_LENGTH = 3000;
const NAME_MAX_LENGTH = 255;

const TARGET_LANGUAGES: TargetLanguage[] = ['ENGLISH', 'POLISH'];

// Closes with true once the backend accepted the request; generation itself runs asynchronously
@Component({
  selector: 'app-create-collection-dialog',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './create-collection-dialog.html',
  styleUrl: './create-collection-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateCollectionDialog {
  private readonly dialogRef = inject<MatDialogRef<CreateCollectionDialog, boolean>>(MatDialogRef);
  private readonly flashcardsService = inject(FlashcardsService);
  private readonly toastr = inject(ToastrService);

  protected readonly textMinLength = TEXT_MIN_LENGTH;
  protected readonly textMaxLength = TEXT_MAX_LENGTH;
  protected readonly languageOptions = TARGET_LANGUAGES.map((value) => ({
    value,
    label: capitalize(value),
  }));

  protected readonly model = signal({
    textToScan: '',
    collectionName: '',
    targetLanguage: 'ENGLISH' as TargetLanguage,
  });
  protected readonly createForm = form(this.model, (path) => {
    required(path.textToScan, { message: 'Paste the text to generate flashcards from' });
    minLength(path.textToScan, TEXT_MIN_LENGTH, {
      message: `The text needs at least ${TEXT_MIN_LENGTH} characters`,
    });
    maxLength(path.textToScan, TEXT_MAX_LENGTH, {
      message: `The text can have at most ${TEXT_MAX_LENGTH} characters`,
    });
    maxLength(path.collectionName, NAME_MAX_LENGTH, {
      message: `The name can have at most ${NAME_MAX_LENGTH} characters`,
    });
  });

  protected async submit(event: Event): Promise<void> {
    event.preventDefault();
    // submit() marks every field as touched, so errors show, and only runs while the form is valid
    await submit(this.createForm, async () => {
      const { textToScan, collectionName, targetLanguage } = this.model();
      try {
        await firstValueFrom(
          this.flashcardsService.createAiFlashcards({
            body: {
              textToScan,
              // Empty means the backend picks a name from the text
              collectionName: collectionName.trim() || null,
              targetLanguage,
            },
          }),
        );
        this.toastr.success('Your flashcards are being generated', 'Collection created');
        this.dialogRef.close(true);
      } catch (error) {
        this.toastr.error(
          (error as HttpErrorResponse).error?.detail ??
            'Could not create the collection, try again later',
          'Creation failed',
        );
      }
    });
  }
}
