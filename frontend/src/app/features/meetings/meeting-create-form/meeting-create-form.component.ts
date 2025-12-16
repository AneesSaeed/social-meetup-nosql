import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MeetingService } from 'src/app/core/services/meeting.service';
import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { ModalRef } from 'src/app/shared/modal/modal-ref';
import { CreateMeetingRequest, Meeting } from 'src/app/core/models/meeting.model';

type MeetingCreateModalData = { userId: string };

@Component({
  selector: 'app-meeting-create-form',
  templateUrl: './meeting-create-form.component.html',
  styleUrls: ['./meeting-create-form.component.scss']
})
export class MeetingCreateFormComponent {
  submitting = false;
  errorMsg = '';

  form = this.fb.group({
    title: ['', Validators.required],
    description: [''],
    eventType: ['sport', Validators.required],
    date: ['', Validators.required],            // datetime-local
    location: ['', Validators.required],
    maxParticipants: [10, [Validators.required, Validators.min(1)]],
    interest: ['', Validators.required],
  });

  constructor(
    private fb: FormBuilder,
    private meetingService: MeetingService,
    private modalRef: ModalRef<Meeting>,
    @Inject(MODAL_DATA) public data: MeetingCreateModalData
  ) {}

  private toLocalDateTime(value: string): string {
    // "YYYY-MM-DDTHH:mm" -> "YYYY-MM-DDTHH:mm:00"
    return value.length === 16 ? `${value}:00` : value;
  }

  submit() {
    this.errorMsg = '';
    if (this.form.invalid) return;

    this.submitting = true;
    const v = this.form.value;

    const payload: CreateMeetingRequest = {
      title: String(v.title),
      description: String(v.description ?? ''),
      eventType: String(v.eventType),
      date: this.toLocalDateTime(String(v.date)),
      location: String(v.location),
      organizer: this.data.userId,
      maxParticipants: Number(v.maxParticipants ?? 10),
      interest: String(v.interest),
    };

    this.meetingService.createMeeting(payload).subscribe({
      next: (created) => this.modalRef.close(created),
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Create meeting failed';
        this.submitting = false;
      }
    });
  }

  cancel() {
    this.modalRef.close();
  }
}
