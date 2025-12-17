import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { SessionService } from 'src/app/core/services/session.service';
import { User } from 'src/app/core/models/user.model';

@Component({
  selector: 'app-profile-drawer',
  templateUrl: './profile-drawer.component.html',
  styleUrls: ['./profile-drawer.component.scss']
})
export class ProfileDrawerComponent {
  @Input() open = false;
  @Output() close = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  user$: Observable<User | null> = this.session.user$;

  constructor(private session: SessionService) {}
}
