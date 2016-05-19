import {Component} from 'angular2/core';
import {Router, RouteConfig, ROUTER_DIRECTIVES} from 'angular2/router';
import {FORM_PROVIDERS} from 'angular2/common';

import '../style/app.scss';

import {UserService} from './shared/user.service';
import {Home} from './home/home.component';
import {Profile} from "./profile/profile.component";
import {Cottages} from "./cottages/cottages.component";
import {CottageService} from "./cottages/shared/cottages.service";
import {PaymentsService} from "./payments/shared/payments.service";
import {PaymentsCentral} from "./payments/payments.component";

/*
 * App Component
 * Top Level Component
 */
@Component({
    selector: 'app', // <app></app>
    providers: [...FORM_PROVIDERS, UserService, CottageService, PaymentsService],
    directives: [...ROUTER_DIRECTIVES],
    pipes: [],
    styles: [require('./app.component.scss')],
    template: require('./app.component.html')
})
@RouteConfig([
    {path: '/', component: Home, as: 'Home', useAsDefault: true},
    {path: '/Profile', component: Profile, as: 'Profile'},
    {path: '/Cottages/...', component: Cottages, as: 'Cottages'},
    {path: '/Payments/...', component: PaymentsCentral, as: 'Payments'}
])
export class App {
    url: string = 'https://github.com/preboot/angular2-webpack';

    constructor(private router: Router) {
    }

    public isRouteActive(route) {
        return this.router.isRouteActive(this.router.generate(route));
    }
}
