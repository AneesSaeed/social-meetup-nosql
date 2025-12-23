import { Component, Input, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { Subscription, catchError, finalize, of } from 'rxjs';
import { SocialGraphApi, RecommendationDto } from 'src/app/core/api/social-graph.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { ToastService } from 'src/app/shared/toast/toast.service';

@Component({
  selector: 'app-suggestions-panel',
  templateUrl: './suggestions-panel.component.html',
  styleUrls: ['./suggestions-panel.component.scss'],
})
export class SuggestionsPanelComponent implements OnInit, OnDestroy, OnChanges {
  @Input() userId!: string;

  loading = false;
  error: string | null = null;
  items: RecommendationDto[] = [];

  private sub = new Subscription();

  constructor(
    private api: SocialGraphApi,
    private meetingEvents: MeetingEventsService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.meetingEvents.updated$.subscribe((m) => {
        if (!this.userId) return;
        if (m.status !== 'COMPLETED') return;
        this.load(true); // refreshed due to completion
      })
    );
  }

  ngOnChanges(): void {
    if (!this.userId) return;
    this.load(false);
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  load(fromCompletion: boolean): void {
    this.loading = true;
    this.error = null;

    this.api.recommendations(this.userId).pipe(
      catchError((e) => {
        const msg = this.extractError(e, 'Failed to load suggestions');
        this.error = msg;
        this.toast.error(msg);
        return of([]);
      }),
      finalize(() => {
        this.loading = false;
      })
    ).subscribe((list) => {
      this.items = list ?? [];

      // Optional: success toast only when this reload is triggered by completing a meeting
      if (fromCompletion) {
        this.toast.success('Suggestions updated');
      }
    });
  }

  trackByUserId(_: number, item: RecommendationDto) {
    return item.userId;
  }

  private extractError(err: any, fallback: string): string {
    return err?.error?.message || err?.error?.error || err?.message || fallback;
  }
}
