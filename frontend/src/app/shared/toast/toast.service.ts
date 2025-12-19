import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info';
export interface ToastMessage {
  text: string;
  type: ToastType;
  durationMs?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _messages = new Subject<ToastMessage>();
  readonly messages$ = this._messages.asObservable();

  show(text: string, type: ToastType = 'info', durationMs = 2500) {
    this._messages.next({ text, type, durationMs });
  }

  success(text: string, durationMs = 2500) { this.show(text, 'success', durationMs); }
  error(text: string, durationMs = 3000) { this.show(text, 'error', durationMs); }
  info(text: string, durationMs = 2500) { this.show(text, 'info', durationMs); }
}
