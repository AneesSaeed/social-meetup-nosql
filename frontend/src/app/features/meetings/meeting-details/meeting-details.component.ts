import { Component, Inject } from '@angular/core';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { SessionService } from 'src/app/core/state/session.service';
import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { ModalRef } from 'src/app/shared/modal/modal-ref';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { UserApi } from 'src/app/core/api/user.api';
import { User } from 'src/app/core/models/user.model';
import { ColorBucketPipe } from '../../../shared/pipes/color-bucket.pipe';
import { DatePipe } from '@angular/common';

type MeetingDetailsData = {
  meeting: Meeting;
  readonly?: boolean;
};

@Component({
    selector: 'app-meeting-details',
    templateUrl: './meeting-details.component.html',
    styleUrls: ['./meeting-details.component.scss'],
    standalone: true,
    imports: [DatePipe, ColorBucketPipe]
})
export class MeetingDetailsComponent {
  meeting: Meeting;
  busy = false;

  readonly = false;
  organizerName: string | null = null;

  constructor(
    @Inject(MODAL_DATA) data: MeetingDetailsData,
    private modalRef: ModalRef,
    private meetingApi: MeetingApi,
    private meetingEvents: MeetingEventsService,
    public session: SessionService,
    private toast: ToastService,
    private userApi: UserApi
  ) {
    this.meeting = data.meeting;
    this.readonly = !!data.readonly;
    this.loadOrganizerName();
  }

  get userId(): string | null {
    return this.session.currentUser?.id ?? null;
  }

  get isParticipant(): boolean {
    return !!this.userId && this.meeting.participants.includes(this.userId);
  }

  get isFull(): boolean {
    return this.meeting.maxParticipants > 0 &&
      this.meeting.participants.length >= this.meeting.maxParticipants;
  }

  get isOrganizer(): boolean {
    return !!this.userId && this.meeting.organizer === this.userId;
  }

  get canComplete(): boolean {
    return this.isOrganizer && this.meeting.status === 'UPCOMING';
  }

  get canCancel(): boolean {
    return this.isOrganizer && this.meeting.status === 'UPCOMING';
  }


  join() {
    if (!this.userId || this.busy || this.isParticipant || this.isFull) return;

    this.busy = true;

    this.meetingApi.join(this.meeting.id, this.userId).subscribe({
      next: (updated) => {
        this.meeting = updated;
        this.busy = false;
        this.toast.success('Successfully joined');
        this.meetingEvents.emitUpdated(updated);
      },
      error: (err) => {
        console.error(err);
        this.busy = false;
        this.toast.error(this.extractError(err, 'Join failed'));
      }
    });
  }

  leave() {
    if (!this.userId || this.busy || !this.isParticipant) return;

    this.busy = true;

    this.meetingApi.leave(this.meeting.id, this.userId).subscribe({
      next: (updated) => {
        this.meeting = updated;
        this.busy = false;
        this.toast.success('Successfully left');
        this.meetingEvents.emitUpdated(updated);
      },
      error: (err) => {
        console.error(err);
        this.busy = false;
        this.toast.error(this.extractError(err, 'Leave failed'));
      }
    });
  }

  complete() {
    if (!this.canComplete || this.busy) return;

    this.busy = true;

    this.meetingApi.complete(this.meeting.id).subscribe({
      next: (updated) => {
        this.meeting = updated;
        this.busy = false;
        this.toast.success('Meeting completed');
        this.meetingEvents.emitUpdated(updated);
      },
      error: (err) => {
        console.error(err);
        this.busy = false;
        this.toast.error(this.extractError(err, 'Complete failed'));
      }
    });
  }

  cancelMeeting() {
    if (!this.canCancel || this.busy) return;

    this.busy = true;

    this.meetingApi.cancel(this.meeting.id).subscribe({
      next: (updated) => {
        this.meeting = updated;
        this.busy = false;
        this.toast.success('Meeting cancelled');
        this.meetingEvents.emitUpdated(updated);
      },
      error: (err) => {
        console.error(err);
        this.busy = false;
        this.toast.error(this.extractError(err, 'Cancel failed'));
      }
    });
  }

  close() {
    this.modalRef.close();
  }

  // -- Helpers --
  private extractError(err: any, fallback: string): string {
    return err?.error?.message || err?.error?.error || err?.message || fallback;
  }

  private loadOrganizerName() {
    if (!this.meeting?.organizer) return;

    this.userApi.getById(this.meeting.organizer).subscribe({
      next: (u: User) => {
        this.organizerName = u.name?.trim() || u.email || u.id;
      },
      error: () => {
        // fall back silently to the id in the UI
        this.organizerName = null;
      }
    });
  }
}
