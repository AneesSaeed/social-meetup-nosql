import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/api/auth.api';
import { SessionService } from '../../../core/state/session.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  errorMessage: string | null = null;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private session: SessionService,
    private router: Router
  ) {}

  submit() {
    if (this.form.invalid) return;

    this.auth.login(this.form.value.email!).subscribe({
      next: user => {
        this.session.setUser(user);
        this.router.navigate(['/home']);
      },
      error: err => {
        this.errorMessage = err.error?.message || "Something went wrong";
      }
    });
  }
}
