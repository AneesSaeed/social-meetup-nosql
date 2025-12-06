import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private session: SessionService
  ) {}

  submit() {
    if (this.form.invalid) return;

    this.auth.login(this.form.value.email!)
      .subscribe(user => this.session.setUser(user));
  }
}
