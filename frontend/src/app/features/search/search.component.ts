import { Component, ElementRef, HostListener, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import {
  Subject,
  debounceTime,
  distinctUntilChanged,
  of,
  switchMap,
  takeUntil,
  catchError,
  tap
} from 'rxjs';

import { SearchService } from 'src/app/core/services/search.service';
import { UserSearchDocument } from 'src/app/core/models/user-search.model';

// Two search modes: default (users) and interests mode
type SearchMode = 'users' | 'interests';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],
})
export class SearchComponent implements OnInit, OnDestroy {
  // Current mode (default: users)
  mode: SearchMode = 'users';

  // Input field state (reactive forms)
  query = new FormControl<string>('', { nonNullable: true });

  // UI state
  loading = false;
  error: string | null = null;
  results: UserSearchDocument[] = [];
  open = false; // dropdown open/closed

  // Used to stop RxJS streams when component is destroyed
  private destroy$ = new Subject<void>();

  // search = backend calls, elRef = detect clicks outside this component
  constructor(
    private search: SearchService,
    private elRef: ElementRef<HTMLElement>
  ) {}

  ngOnInit(): void {
    // Listen to input changes
    this.query.valueChanges
      .pipe(
        // Wait 250ms after typing stops (avoid too many requests)
        debounceTime(250),

        // Ignore same value twice
        distinctUntilChanged(),

        // Update UI state before calling backend
        tap(() => {
          this.error = null;

          const trimmed = this.query.value.trim();
          const ready = this.isReadyToSearch(trimmed);

          this.open = ready; // show panel only if ready
          this.loading = ready;  // show loading only if ready
          if (!ready) this.results = []; // clear results if query not valid
        }),

        // Do the search; cancels previous request when typing again
        switchMap((value) => {
          const input = value.trim();
          // Not enough input => no backend call
          if (!this.isReadyToSearch(input)) return of<UserSearchDocument[]>([]);

          // Mode 1: search users by name/bio
          if (this.mode === 'users') {
            return this.search.searchUsers(input).pipe(
              // If backend fails, show error and return empty list
              catchError(() => {
                this.error = 'Search failed';
                return of<UserSearchDocument[]>([]);
              })
            );
          }

          // Mode 2: search by interests (comma separated)
          const interests = this.parseInterests(input);
          if (interests.length === 0) return of<UserSearchDocument[]>([]);

           // AND search: user must have ALL interests
          return this.search.searchByInterestsAll(interests).pipe(
            catchError(() => {
              this.error = 'Search failed';
              return of<UserSearchDocument[]>([]);
            })
          );
        }),

        // Apply results to UI
        tap((res) => {
          this.results = res;
          this.loading = false;
          this.open = this.isReadyToSearch(this.query.value.trim());
        }),

        // Stop stream when component is destroyed (prevents memory leaks)
        takeUntil(this.destroy$)
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    // Trigger takeUntil to unsubscribe
    this.destroy$.next();
    this.destroy$.complete();
  }

  setMode(mode: SearchMode): void {
    // No-op if same mode
    if (this.mode === mode) return;

    // Change mode
    this.mode = mode;

    // Re-run search with same text in the new mode
    const current = this.query.value;
    this.query.setValue(current);
  }

  clear(): void {
    // Reset everything
    this.query.setValue('');
    this.results = [];
    this.error = null;
    this.loading = false;
    this.open = false;
  }

  // Close dropdown when clicking outside the component
  @HostListener('document:mousedown', ['$event'])
  onDocMouseDown(e: MouseEvent): void {
    if (!this.elRef.nativeElement.contains(e.target as Node)) {
      this.open = false;
    }
  }

  // Helps Angular render lists efficiently
  trackByUserId(_: number, u: UserSearchDocument): string {
    return u.userId;
  }

  // Input placeholder depends on the mode
  placeholder(): string {
    return this.mode === 'users'
      ? 'Search users by name or bio…'
      : 'Search by interests (comma separated)…';
  }

   // Decide if we should call backend
  public isReadyToSearch(input: string): boolean {
    // Users mode: need at least 2 chars
    if (this.mode === 'users') return input.length >= 2;

    // Interests mode: need at least 1 token
    return this.parseInterests(input).length >= 1;
  }

  private parseInterests(input: string): string[] {
    // "music, java, chess" => ["music","java","chess"]
    return input
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);
  }
}
