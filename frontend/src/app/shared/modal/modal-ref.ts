// A small helper object given to the modal content component.
// The content calls modalRef.close(...) to close the modal and optionally return a result.
export class ModalRef<T = any> {

  // Prevent closing twice (double click, multiple events, etc.)
  private _closed = false;

  // closeFn is provided by the BaseModalComponent to notify the parent.
  constructor(private closeFn: (result?: T) => void) {}

  // Closes the modal and optionally returns a value (e.g. the created Meeting).
  close(result?: T) {
    if (this._closed) return; // ignore if already closed
    this._closed = true;
    this.closeFn(result);
  }
}
