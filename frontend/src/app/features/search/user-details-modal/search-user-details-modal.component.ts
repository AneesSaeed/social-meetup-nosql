import { Component, Inject, OnInit } from '@angular/core';
import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { UserApi } from 'src/app/core/api/user.api';
import { User } from 'src/app/core/models/user.model';
import { take } from 'rxjs';

@Component({
  selector: 'app-search-user-details-modal',
  templateUrl: './search-user-details-modal.component.html',
  styleUrls: ['./search-user-details-modal.component.scss']
})
export class SearchUserDetailsModalComponent implements OnInit {
  loading = false;
  errorMsg = '';
  user: User | null = null;

  constructor(
    @Inject(MODAL_DATA) public data: { userId: string },
    private userApi: UserApi
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
}
