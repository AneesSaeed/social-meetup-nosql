import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SessionService } from 'src/app/core/services/session.service';
import { HostListener } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  user$ = this.session.user$;
  isDrawerOpen = false;

  constructor(private session: SessionService, private router: Router) {}

  openDrawer() { this.isDrawerOpen = true; }
  closeDrawer() { this.isDrawerOpen = false; }

  logout() {
    this.session.clearUser();
    this.isDrawerOpen = false;
    this.router.navigate(['/login']);
  }

  @HostListener('document:keydown.escape')
  onEsc() {
    this.isDrawerOpen = false;
  }
}
