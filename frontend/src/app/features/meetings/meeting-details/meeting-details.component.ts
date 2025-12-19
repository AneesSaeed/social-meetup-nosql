import { Component, Inject } from '@angular/core';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingService } from 'src/app/core/api/meeting.api';
import { SessionService } from 'src/app/core/state/session.service';
import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { ModalRef } from 'src/app/shared/modal/modal-ref';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';

type MeetingDetailsData = { meeting: Meeting };

@Component({
  selector: 'app-meeting-details',
  templateUrl: './meeting-details.component.html',
  styleUrls: ['./meeting-details.component.scss']
})
export class MeetingDetailsComponent {
  meeting: Meeting;
  busy = false;

  constructor(
    @Inject(MODAL_DATA) data: MeetingDetailsData,
    private modalRef: ModalRef,
    private meetingService: MeetingService,
    private meetingEvents: MeetingEventsService,
    public session: SessionService,
    private toast: ToastService
  ) {
    this.meeting = data.meeting;
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

  private extractError(err: any, fallback: string): string {
    return err?.error?.message || err?.error?.error || err?.message || fallback;
  }

  join() {
    if (!this.userId || this.busy || this.isParticipant || this.isFull) return;

    this.busy = true;

    this.meetingService.join(this.meeting.id, this.userId).subscribe({
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

    this.meetingService.leave(this.meeting.id, this.userId).subscribe({
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

  close() {
    this.modalRef.close();
  }
}
