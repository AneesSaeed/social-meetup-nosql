import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { catchError, filter, Observable, of, Subscription, switchMap } from 'rxjs';
import { SessionService } from 'src/app/core/state/session.service';
import { User } from 'src/app/core/models/user.model';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { UserApi } from 'src/app/core/api/user.api';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { ColorBucketPipe } from '../../shared/pipes/color-bucket.pipe';
import { NgClass, AsyncPipe } from '@angular/common';

@Component({
    selector: 'app-profile-drawer',
    templateUrl: './profile-drawer.component.html',
    styleUrls: ['./profile-drawer.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ReactiveFormsModule, NgClass, AsyncPipe, ColorBucketPipe]
})
export class ProfileDrawerComponent implements OnInit, OnDestroy {
  @Input() open = false;
  @Output() close = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  user$: Observable<User | null> = this.session.user$;

  private sub = new Subscription();

  editingBio = false;
  editingInterests = false;

  bioCtrl = new FormControl<string>('', { nonNullable: true });
  interestCtrl = new FormControl<string>('', { nonNullable: true });

  savingBio = false;
  savingInterests = false;

  constructor(
    private session: SessionService,
    private meetingEvents: MeetingEventsService,
    private userApi: UserApi,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.meetingEvents.updated$.pipe(
        // Only react when a meeting becomes COMPLETED
        filter(m => m.status === 'COMPLETED'),

        // We need a logged-in user id to refresh score
        filter(() => !!this.session.currentUser?.id),

        // fetch latest user from Mongo (has totalPoints / totalMeetings)
        switchMap(() =>
          this.userApi.getById(this.session.currentUser!.id).pipe(
            // ignore errors but keep listening
            catchError(() => of(null))
          )
        )
      ).subscribe((fresh) => {
        if (!fresh) return;
        this.session.updateCurrentUser(fresh);
      })
    );
  }

  ngOnDestroy(): void {
    // Stop listening to meeting events when component is destroyed
    this.sub.unsubscribe();
  }

  // call this when user$ emits (or when opening drawer)
  private initFormFromUser(user: User | null) {
    this.bioCtrl.setValue(user?.bio ?? '', { emitEvent: false });
  }

  toggleBioEdit(user: User | null) {
    this.editingBio = !this.editingBio;
    if (this.editingBio) this.initFormFromUser(user);
  }

  toggleInterestsEdit() {
    this.editingInterests = !this.editingInterests;
    this.interestCtrl.setValue('', { emitEvent: false });
  }

  addInterest(user: User)  {
    const v = this.interestCtrl.value.trim();
    if (!v) return;

    const current = (user.interests ?? []) as string[];
    const exists = current.some(i => i.toLowerCase() === v.toLowerCase());
    if (exists) {
      this.interestCtrl.setValue('');
      return;
    }

    const updated = [...current, v];
    this.saveInterests(updated);
    this.interestCtrl.setValue('');
  }

  removeInterest(user: User, interest: string) {
    const updated = (user.interests ?? []).filter(i => i !== interest);
    this.saveInterests(updated);
  }

  saveBio() {
    const id = this.session.currentUser?.id;
    if (!id || this.savingBio) return;

    this.savingBio = true;
    const bio = this.bioCtrl.value;

    this.userApi.updateProfile(id, { bio }).pipe(
      catchError((e) => {
        this.toast.error(e?.error?.message ?? 'Failed to update bio');
        return of(null);
      })
    ).subscribe((fresh) => {
      this.savingBio = false;
      if (!fresh) return;

      this.session.updateCurrentUser(fresh);
      this.editingBio = false;
      this.toast.success('Bio updated');
    });
  }

  private saveInterests(interests: string[]) {
    const id = this.session.currentUser?.id;
    if (!id || this.savingInterests) return;

    this.savingInterests = true;

    this.userApi.updateProfile(id, { interests }).pipe(
      catchError((e) => {
        this.toast.error(e?.error?.message ?? 'Failed to update interests');
        return of(null);
      })
    ).subscribe((fresh) => {
      this.savingInterests = false;
      if (!fresh) return;

      this.session.updateCurrentUser(fresh);
      this.toast.success('Interests updated');
    });
  }
}
