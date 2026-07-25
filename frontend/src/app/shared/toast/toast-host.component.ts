import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ToastMessage, ToastService } from './toast.service';

@Component({
    selector: 'app-toast-host',
    template: `
    @if (current) {
      <div class="toast-wrap" [class.error]="current.type==='error'"
        [class.success]="current.type==='success'" [class.info]="current.type==='info'">
        {{ current.text }}
      </div>
    }
    `,
    styleUrls: ['./toast-host.component.scss'],
    standalone: true
})
export class ToastHostComponent implements OnInit, OnDestroy {
  current: ToastMessage | null = null;
  private sub?: Subscription;
  private timer: any;

  constructor(private toast: ToastService) {}

  ngOnInit() {
    this.sub = this.toast.messages$.subscribe(msg => {
      this.current = msg;
      if (this.timer) clearTimeout(this.timer);
      this.timer = setTimeout(() => (this.current = null), msg.durationMs ?? 2500);
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
    if (this.timer) clearTimeout(this.timer);
  }
}
