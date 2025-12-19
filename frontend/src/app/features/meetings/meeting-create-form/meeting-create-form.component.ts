import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Subscription } from 'rxjs';
import { MeetingService } from 'src/app/core/api/meeting.api';
import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { ModalRef } from 'src/app/shared/modal/modal-ref';
import { CreateMeetingRequest, Meeting } from 'src/app/core/models/meeting.model';
import { SessionService } from 'src/app/core/state/session.service';
import { colorBucket } from 'src/app/shared/utils/colors-hash';

type MeetingCreateModalData = { userId: string };

function minArrayLength(min: number) {
  return (control: AbstractControl): ValidationErrors | null => {
    const arr = control as FormArray;
    return arr && arr.length >= min ? null : { minArrayLength: { required: min, actual: arr?.length ?? 0 } };
  };
}

@Component({
  selector: 'app-meeting-create-form',
  templateUrl: './meeting-create-form.component.html',
  styleUrls: ['./meeting-create-form.component.scss']
})
export class MeetingCreateFormComponent implements OnInit, OnDestroy {
  submitting = false;
  errorMsg = '';

  private sub = new Subscription();

  form = this.fb.group({
    title: ['', Validators.required],
    description: [''],
    eventType: ['sport', Validators.required],
    date: ['', Validators.required],
    location: ['', Validators.required],
    maxParticipants: [10, [Validators.required, Validators.min(1)]],
    interests: this.fb.array<string>([], [minArrayLength(1)]),
  });

  constructor(
    private fb: FormBuilder,
    private meetingService: MeetingService,
    private modalRef: ModalRef<Meeting>,
    private session: SessionService,
    @Inject(MODAL_DATA) public data: MeetingCreateModalData
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.session.user$.subscribe(user => {
        if (!user) return;
        if (this.interestsArray.length > 0) return;

        (user.interests ?? []).forEach((i: string) => {
          const v = (i ?? '').trim();
          if (v) this.interestsArray.push(this.fb.control(v));
        });
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  get interestsArray(): FormArray {
    return this.form.get('interests') as FormArray;
  }

  addInterest(value: string) {
    const v = (value ?? '').trim();
    if (!v) return;

    const exists = this.interestsArray.controls
      .some(c => String(c.value).toLowerCase() === v.toLowerCase());
    if (exists) return;

    this.interestsArray.push(this.fb.control(v));
    this.interestsArray.markAsDirty();
    this.interestsArray.updateValueAndValidity();
  }

  removeInterest(index: number) {
    if (index < 0 || index >= this.interestsArray.length) return;
    this.interestsArray.removeAt(index);
    this.interestsArray.markAsDirty();
    this.interestsArray.updateValueAndValidity();
  }

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
      interests: (this.interestsArray.value as string[]).map(s => String(s).trim()).filter(Boolean),
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
