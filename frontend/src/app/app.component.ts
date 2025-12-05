import { Component } from '@angular/core';
import { TestService } from './test.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  result = '';

  constructor(private testService: TestService) {}

  callTest() {
    this.testService.getTest().subscribe(res => {
      this.result = res;
    });
  }
}
