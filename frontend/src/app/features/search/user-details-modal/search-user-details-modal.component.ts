import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { take } from 'rxjs';

import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { UserApi } from 'src/app/core/api/user.api';
import { User } from 'src/app/core/models/user.model';

import { SessionService } from 'src/app/core/state/session.service';
import { MeetingCreateFormComponent } from '../../meetings/meeting-create-form/meeting-create-form.component';
import { ColorBucketPipe } from '../../../shared/pipes/color-bucket.pipe';
import { BaseModalComponent } from '../../../shared/modal/base-modal.component';

@Component({
    selector: 'app-search-user-details-modal',
    templateUrl: './search-user-details-modal.component.html',
    styleUrls: ['./search-user-details-modal.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [BaseModalComponent, ColorBucketPipe]
})
export class SearchUserDetailsModalComponent implements OnInit {
  loading = false;
  errorMsg = '';
  user: User | null = null;

  // meeting modal (nested)
  isMeetOpen = false;
  meetTitle = 'Create 1:1 meeting';
  meetComponent = MeetingCreateFormComponent;
  meetData: any = null;

  constructor(
    @Inject(MODAL_DATA) public data: { userId: string },
    private userApi: UserApi,
    private session: SessionService
  ) {}

  ngOnInit(): void {
    const id = this.data?.userId;

    if (!id) {
      this.errorMsg = 'Missing user id';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.user = null;

    this.userApi.getById(id).pipe(take(1)).subscribe({
      next: (u) => {
        this.user = u;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to load user details';
        this.loading = false;
      }
    });
  }

  meet(u: User) {
    const me = this.session.currentUser;
    if (!me?.id) return;

    const interests = this.unionInterests(me.interests ?? [], u.interests ?? []);

    this.meetData = {
      userId: me.id,              // organizer (logged-in user)
      oneToOne: true,             // enable 1:1 behavior in MeetingCreateForm
      otherUserId: u.id,          // optional: if backend later supports "invitee"
      seedInterests: interests    // union set
    };

    this.isMeetOpen = true;
  }

  onMeetClosed() {
    this.isMeetOpen = false;
  }

  private unionInterests(a: string[], b: string[]): string[] {
    const seen = new Set<string>();
    const out: string[] = [];

    [...a, ...b].forEach(raw => {
      const v = String(raw ?? '').trim();
      if (!v) return;

      const key = v.toLowerCase();
      if (seen.has(key)) return;

      seen.add(key);
      out.push(v);
    });

    return out;
  }
}
