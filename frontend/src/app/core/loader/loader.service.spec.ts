import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { LoaderService } from './loader.service';

describe('LoaderService', () => {
  let loader: LoaderService;

  beforeEach(() => {
    vi.useFakeTimers();
    loader = TestBed.inject(LoaderService);
  });
  afterEach(() => vi.useRealTimers());

  it('is busy right away but waits for the threshold before showing the spinner', () => {
    loader.show();
    expect(loader.busy()).toBe(true);
    expect(loader.loading()).toBe(false);

    vi.advanceTimersByTime(400);
    expect(loader.loading()).toBe(true);
  });

  it('never shows the spinner for a request that finishes before the threshold', () => {
    loader.show();
    vi.advanceTimersByTime(300);
    loader.hide();
    vi.advanceTimersByTime(1000);

    expect(loader.busy()).toBe(false);
    expect(loader.loading()).toBe(false);
  });

  it('keeps the spinner up for the minimum duration once shown', () => {
    loader.show();
    vi.advanceTimersByTime(450);
    loader.hide();
    expect(loader.busy()).toBe(false);
    expect(loader.loading()).toBe(true);

    vi.advanceTimersByTime(249);
    expect(loader.loading()).toBe(true);
    vi.advanceTimersByTime(1);
    expect(loader.loading()).toBe(false);
  });

  it('keeps the spinner up when a new request starts during the minimum duration', () => {
    loader.show();
    vi.advanceTimersByTime(400);
    loader.hide();
    loader.show();
    vi.advanceTimersByTime(1000);
    expect(loader.loading()).toBe(true);

    loader.hide();
    expect(loader.loading()).toBe(false);
  });
});
