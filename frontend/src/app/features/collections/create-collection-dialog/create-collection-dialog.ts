import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { form, FormField, maxLength, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ToastrService } from 'ngx-toastr';
import { firstValueFrom } from 'rxjs';
import { CollectionsService } from '../../../api/services/collections.service';

// Same limit as the backend's CreateCollectionDto validation
const NAME_MAX_LENGTH = 255;

// Creates an empty collection; flashcards are generated into it separately. Closes with true once created
@Component({
  selector: 'app-create-collection-dialog',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './create-collection-dialog.html',
  styleUrl: './create-collection-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateCollectionDialog {
  private readonly dialogRef = inject<MatDialogRef<CreateCollectionDialog, boolean>>(MatDialogRef);
  private readonly collectionsService = inject(CollectionsService);
  private readonly toastr = inject(ToastrService);

  protected readonly model = signal({ name: '' });
  protected readonly createForm = form(this.model, (path) => {
    maxLength(path.name, NAME_MAX_LENGTH, {
      message: `The name can have at most ${NAME_MAX_LENGTH} characters`,
    });
  });

  protected async submit(event: Event): Promise<void> {
    event.preventDefault();
    // submit() marks every field as touched, so errors show, and only runs while the form is valid
    await submit(this.createForm, async () => {
      try {
        await firstValueFrom(
          this.collectionsService.createCollection({
            // Empty means the backend generates a name
            body: { name: this.model().name.trim() || null },
          }),
        );
        this.toastr.success('Your new collection is ready', 'Collection created');
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
