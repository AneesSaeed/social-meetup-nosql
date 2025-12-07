import { NgFor } from '@angular/common';
import { Component } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from 'src/app/core/services/auth.service';
import { SessionService } from 'src/app/core/services/session.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  errorMessage: string | null = null;

  // Inject services needed by this component:
  // - FormBuilder: to build forms easily
  // - AuthService: to call the /register API
  // - SessionService: to store the user after registration
  // - Router: to navigate the user
  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private session: SessionService,
    private router: Router

  ) {}

  // Create a reactive form with fields: name, email, bio, and an array of interests.
  form = this.fb.group({
    name: ['', Validators.required],                       // Required text field
    email: ['', [Validators.required, Validators.email]],   // Must be required + valid email
    bio: ['', Validators.required],                        // Required text field
    interests: this.fb.array<string>([], Validators.minLength(1))
  });

  // Convenience getter so template can easily access the interests FormArray
  get interestsArray(): FormArray {
    return this.form.get('interests') as FormArray;
  }

  // Add a new interest as a form control inside the FormArray
  addInterest(interest: string) {
    if (!interest.trim()) return; // Ignore empty values
    this.interestsArray.push(this.fb.control(interest));
  }

  // Remove interest at the given index
  removeInterest(i: number) {
    this.interestsArray.removeAt(i);
  }

  // Called when the user submits the form
  submit() {
    if (this.form.invalid) return; // Stop if form isn't valid

    // Extract form values
    const { name, email, bio, interests } = this.form.value;

    // Call the API to register the user
    // register(...) returns an Observable<User>.
    // subscribe(...) waits for the backend response.
    this.auth.register(name!, email!, bio!, interests as string[]).subscribe({
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
