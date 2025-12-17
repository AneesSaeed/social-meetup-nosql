import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';



import { AppComponent } from './app.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { HomeComponent } from './features/home/home.component';
import { SearchComponent } from './features/search/search.component';
import { HeaderComponent } from './features/header/header.component';
import { BaseModalComponent } from './shared/modal/base-modal.component';
import { MeetingCreateFormComponent } from './features/meetings/meeting-create-form/meeting-create-form.component';
import { CreateMeetingButtonComponent } from './features/meetings/create-meeting-button/create-meeting-button.component';
import { UpcomingMeetingsRowComponent } from './features/meetings/upcoming-meetings-row/upcoming-meetings-row.component';
import { MeetingDetailsComponent } from './features/meetings/meeting-details/meeting-details.component';
import { ColorBucketPipe } from './shared/pipes/color-bucket.pipe';
import { ProfileDrawerComponent } from './features/profile-drawer/profile-drawer.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    HomeComponent,
    SearchComponent,
    HeaderComponent,
    BaseModalComponent,
    MeetingCreateFormComponent,
    CreateMeetingButtonComponent,
    UpcomingMeetingsRowComponent,
    MeetingDetailsComponent,
    ColorBucketPipe,
    ProfileDrawerComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule, // required for form
    CommonModule         // needed for *ngFor and Angular directives
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
