import { Component, OnInit } from '@angular/core';
import { AccountService } from '@app/_services';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { tap } from 'rxjs/operators'

import { DoorDragonComponent } from '@app/_components';

const START_TIME =  1610730000000;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  returnUrl!: string;
  form!: FormGroup;
  loading = false;
  submitted = false;
  submissionError?: string;
  justLoggedOut = false;
  showRealTitle = false;

  constructor(
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private modalService: NgbModal,
  ) {
    const now = Date.now();
    this.showRealTitle = now >= START_TIME;
    if (!this.showRealTitle) {
      setTimeout(() => this.showRealTitle = true, START_TIME - now);
    }
  }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/map';
    this.justLoggedOut = !!this.route.snapshot.queryParams['loggedOut'];
  }

  get f() { return this.form.controls; }

  login() {
    this.submitted = true;
    this.submissionError = undefined;
    // todo alert service for errors
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.accountService.login(this.f.username.value, this.f.password.value).subscribe( retval => {
      console.log('logged in');
      this.router.navigate([this.returnUrl]);
    }, error => {
      this.loading = false;
      this.submissionError = error;
    });
  }

  onForgot() {
    const modalRef = this.modalService.open(DoorDragonComponent, {size: 'lg'});
    const doorDragon = modalRef.componentInstance as DoorDragonComponent;
    doorDragon.style = 'reset';
    modalRef.closed.pipe (
      tap(result => {
        if (result == "success") {
          window.open('http://reg.arisia.org/kiosk/web_reg/', '_blank');
        }
      }),
    ).subscribe();
  }
}
