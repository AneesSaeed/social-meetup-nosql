import {
  Component,
  EventEmitter,
  HostListener,
  Injector,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  Type
} from '@angular/core';
import { MODAL_DATA } from './modal.tokens';
import { ModalRef } from './modal-ref';
import { NgComponentOutlet } from '@angular/common';

@Component({
    selector: 'app-base-modal',
    templateUrl: './base-modal.component.html',
    styleUrls: ['./base-modal.component.scss'],
    imports: [NgComponentOutlet]
})
export class BaseModalComponent implements OnChanges {
  @Input() title = '';   // Title shown in the modal header
  @Input() component!: Type<any>; // The component class to render inside the modal (dynamic component)
  @Input() data: any;   // Data passed to the inner component (injected using MODAL_DATA)

  @Output() closed = new EventEmitter<any>(); // Emits when the modal is closed (optionally with a result)

  childInjector!: Injector;  // Injector used to create the inner component with extra providers (data + modalRef)
  private modalRef!: ModalRef; // Reference used to close the modal from inside the content component

  constructor(private injector: Injector) {}

  // Rebuild the injector whenever the content component or its data changes
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['component'] || changes['data']) {
      // Create a ModalRef that will emit the "closed" event when close() is called
      this.modalRef = new ModalRef((result) => this.closed.emit(result));

      // Create a child injector so the inner component can inject:
      // - MODAL_DATA (the passed data)
      // - ModalRef (so it can close itself)
      this.childInjector = Injector.create({
        parent: this.injector, // keep access to app-wide services (HttpClient, etc.)
        providers: [
          { provide: MODAL_DATA, useValue: this.data },
          { provide: ModalRef, useValue: this.modalRef }
        ]
      });
    }
  }

  // Close the modal (backdrop click, X button, escape key)
  close() {
    this.modalRef?.close();
  }

  // Close when user presses Escape anywhere
  @HostListener('document:keydown.escape')
  onEsc() {
    this.close();
  }
}
