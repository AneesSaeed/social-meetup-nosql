import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './features/header/header.component';
import { ToastHostComponent } from './shared/toast/toast-host.component';

@Component({
    selector: 'app-root',
    imports: [
        HeaderComponent,
        ToastHostComponent,
        RouterOutlet
    ],
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {
  hideHeader = false;

  constructor(private router: Router) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(event => {
        const nav = event as NavigationEnd;
        this.hideHeader = ['/login', '/register'].includes(nav.urlAfterRedirects);
      });
  }
}
