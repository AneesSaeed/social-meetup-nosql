import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SessionService } from './core/services/session.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(private router: Router, private session: SessionService) {
    if (!this.session.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
  }
}
