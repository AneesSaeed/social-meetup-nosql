import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventService } from './services/event.service';
import { MeetingService } from './services/meeting.service';
import { SearchService } from './services/search.service';
import { UserService } from './services/user.service';
import { EventResponse, MeetingResponse, UserResponse } from './models';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  userForm!: FormGroup;
  meetingForm!: FormGroup;
  eventForm!: FormGroup;
  searchQuery = '';
  searchResults: any[] = [];

  users: UserResponse[] = [];
  meetings: MeetingResponse[] = [];
  events: EventResponse[] = [];
  score: number | null = null;

  loading = false;
  message = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private meetingService: MeetingService,
    private eventService: EventService,
    private searchService: SearchService
  ) {}

  ngOnInit(): void {
    this.userForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      bio: [''],
      interests: ['', Validators.required],
    });

    this.meetingForm = this.fb.group({
      user1: ['', Validators.required],
      user2: ['', Validators.required],
      interest: ['', Validators.required],
      location: ['', Validators.required],
      points: [10, [Validators.required, Validators.min(1)]],
      description: [''],
    });

    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      eventType: ['', Validators.required],
      date: ['', Validators.required],
      location: ['', Validators.required],
      organizer: ['', Validators.required],
      maxParticipants: [10],
      interests: [''],
    });

    this.loadUsers();
    this.loadEvents();
  }

  loadUsers() {
    this.userService.getAll().subscribe({
      next: (users) => (this.users = users),
      error: () => (this.message = 'Erreur lors du chargement des utilisateurs')
    });
  }

  loadMeetingsFor(userId: string) {
    if (!userId) return;
    this.meetingService.getForUser(userId).subscribe({
      next: (ms) => (this.meetings = ms),
      error: () => (this.message = 'Erreur lors du chargement des rencontres')
    });
    this.meetingService.getScore(userId).subscribe({
      next: (s) => (this.score = s),
      error: () => (this.score = null)
    });
  }

  loadEvents() {
    this.eventService.list().subscribe({
      next: (evts) => (this.events = evts),
      error: () => (this.message = 'Erreur lors du chargement des événements')
    });
  }

  submitUser() {
    if (this.userForm.invalid) return;
    this.loading = true;
    const payload = {
      ...this.userForm.value,
      interests: this.userForm.value.interests.split(',').map((s: string) => s.trim()).filter(Boolean),
    };
    this.userService.create(payload).subscribe({
      next: (u) => {
        this.message = `Utilisateur créé : ${u.name}`;
        this.userForm.reset();
        this.loadUsers();
      },
      error: () => (this.message = 'Erreur lors de la création utilisateur'),
      complete: () => (this.loading = false)
    });
  }

  submitMeeting() {
    if (this.meetingForm.invalid) return;
    const payload = {
      participants: [this.meetingForm.value.user1, this.meetingForm.value.user2],
      interest: this.meetingForm.value.interest,
      location: this.meetingForm.value.location,
      points: this.meetingForm.value.points,
      description: this.meetingForm.value.description
    };
    this.meetingService.create(payload).subscribe({
      next: (m) => {
        this.message = `Rencontre créée (${m.id})`;
        this.loadMeetingsFor(payload.participants[0]);
      },
      error: () => (this.message = 'Erreur lors de la création de la rencontre')
    });
  }

  submitEvent() {
    if (this.eventForm.invalid) return;
    const payload = {
      ...this.eventForm.value,
      date: this.eventForm.value.date,
      interests: this.eventForm.value.interests
        ? this.eventForm.value.interests.split(',').map((s: string) => s.trim()).filter(Boolean)
        : [],
    };
    this.eventService.create(payload).subscribe({
      next: (e) => {
        this.message = `Événement créé (${e.title})`;
        this.loadEvents();
      },
      error: () => (this.message = 'Erreur lors de la création de l’événement')
    });
  }

  doSearch() {
    if (!this.searchQuery) return;
    this.searchService.searchUsers(this.searchQuery).subscribe({
      next: (r) => (this.searchResults = r),
      error: () => (this.message = 'Erreur lors de la recherche')
    });
  }

  onSelectUser(userId: string) {
    this.loadMeetingsFor(userId);
  }
}
