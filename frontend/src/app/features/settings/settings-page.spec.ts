import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SettingsStore } from '../../state/settings.store';
import { SettingsPage } from './settings-page';

describe('SettingsPage', () => {
  let fixture: ComponentFixture<SettingsPage>;
  let store: InstanceType<typeof SettingsStore>;

  beforeEach(async () => {
    localStorage.clear();
    fixture = TestBed.createComponent(SettingsPage);
    store = TestBed.inject(SettingsStore);
    await fixture.whenStable();
  });

  function element(): HTMLElement {
    return fixture.nativeElement;
  }

  function button(text: string): HTMLButtonElement {
    return Array.from(element().querySelectorAll<HTMLButtonElement>('button'))
      .find((b) => b.textContent?.trim() === text)!;
  }

  function previewFace(side: 'front' | 'back'): HTMLElement {
    return element().querySelector<HTMLElement>(`.preview app-flashcard .${side}`)!;
  }

  function previewFlipped(): boolean {
    return element().querySelector('.preview .flip-card')!.classList.contains('flipped');
  }

  async function click(target: HTMLElement): Promise<void> {
    target.click();
    await fixture.whenStable();
  }

  it('shows one preview card labelled with its sides', () => {
    expect(element().querySelectorAll('.preview app-flashcard')).toHaveLength(1);
    expect(previewFace('front').textContent?.trim()).toBe('Front');
    expect(previewFace('back').textContent?.trim()).toBe('Back');
  });

  it('previews the chosen colours', async () => {
    store.setFrontBackground('#ff0000');
    store.setBackText('dark');
    await fixture.whenStable();

    expect(previewFace('front').style.background).toBe('rgb(255, 0, 0)');
    expect(previewFace('back').style.color).toBe('var(--fg-grey-900)');
  });

  it('edits the side the preview card shows, switching when the card is flipped', async () => {
    await click(button('White'));
    expect(store.frontText()).toBe('light');

    await click(element().querySelector<HTMLElement>('.preview .flip-card')!);
    expect(previewFlipped()).toBe(true);
    expect(element().textContent).toContain('Back background');
    await click(button('Black'));

    expect(store.backText()).toBe('dark');
    expect(store.frontText()).toBe('light');
  });

  it('offers to go back to the default gradient only after a colour was picked', async () => {
    expect(button('Use default')).toBeUndefined();

    store.setFrontBackground('#123456');
    await fixture.whenStable();
    await click(button('Use default'));

    expect(store.frontBackground()).toBeNull();
  });
});
