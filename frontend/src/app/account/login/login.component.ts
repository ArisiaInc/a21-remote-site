import { Component, OnInit } from '@angular/core';
import { AccountService } from '@app/_services';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

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

  constructor(
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  get f() { return this.form.controls; }

  login() {
    this.submitted = true;
    // todo alert service for errors
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.accountService.login(this.f.username.value, this.f.password.value).subscribe( retval => {
      console.log('logged in');
      this.router.navigate([this.returnUrl]);
    }, error => {
      // TODO show something to the user here about 401s?
      console.error(error);
      this.loading = false;
    });
  }

}
