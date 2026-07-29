import { Component, Input, OnChanges, OnDestroy, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Subscription, catchError, finalize, of } from 'rxjs';
import { SocialGraphApi, NetworkDto } from 'src/app/core/api/social-graph.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { ToastService } from 'src/app/shared/toast/toast.service';

import { SearchUserDetailsModalComponent } from 'src/app/features/search/user-details-modal/search-user-details-modal.component';
import { BaseModalComponent } from '../../../shared/modal/base-modal.component';

@Component({
    selector: 'app-suggestions-panel',
    templateUrl: './suggestions-panel.component.html',
    styleUrls: ['./suggestions-panel.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [BaseModalComponent]
})
export class SuggestionsPanelComponent implements OnInit, OnDestroy, OnChanges {
  @Input() userId!: string;

  loading = false;
  error: string | null = null;
  items: NetworkDto[] = [];

  // modal
  isOpen = false;
  modalTitle = 'User details';
  modalComponent = SearchUserDetailsModalComponent;
  modalData: any = null;

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
        this.load(true);
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

  openUser(suggestedUserId: string): void {
    this.modalData = { userId: suggestedUserId };
    this.isOpen = true;
  }

  onModalClosed(): void {
    this.isOpen = false;
  }

  load(fromCompletion: boolean): void {
    this.loading = true;
    this.error = null;

    this.api.network(this.userId).pipe(
      catchError((e) => {
        const msg = this.extractError(e, 'Failed to load suggestions');
        this.error = msg;
        this.toast.error(msg);
        return of([]);
      }),
      finalize(() => { this.loading = false; })
    ).subscribe((list) => {
      this.items = list ?? [];
      if (fromCompletion) this.toast.success('Suggestions updated');
    });
  }

  trackByUserId(_: number, item: NetworkDto) {
    return item.userId;
  }

  private extractError(err: any, fallback: string): string {
    return err?.error?.message || err?.error?.error || err?.message || fallback;
  }
}
