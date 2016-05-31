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
import {RecommendationService} from "./members/shared/recommendation.service";
import {PaymentsCentral} from "./payments/payments.component";
import {Members} from "./members/members.component";
import {Notifications} from "./notifications/notifications.component";
import {NotificationsService} from "./notifications/shared/notifications.service.ts";
import {Member} from "./member/member.component.ts";

/*
 * App Component
 * Top Level Component
 */
@Component({
    selector: 'app', // <app></app>
    providers: [...FORM_PROVIDERS, UserService, CottageService, PaymentsService, RecommendationService, NotificationsService],
    directives: [...ROUTER_DIRECTIVES, Notifications],
    pipes: [],
    styles: [require('./app.component.scss')],
    template: require('./app.component.html')
})

@RouteConfig([
    {path: '/Profile', component: Profile, as: 'Profile'},
    {path: '/Cottages/...', component: Cottages, as: 'Cottages', useAsDefault: true},
    {path: '/Payments/...', component: PaymentsCentral, as: 'Payments'},
    {path: '/Members/', component: Members, as: 'Members'},
    {path: '/Member/:id', component: Member, as: 'Member'},
    {path: '/*path', redirectTo:['Cottages'] }
])

export class App {
    balance: number;
    isAdministrator = false;
    isCandidate = true;

    constructor(private router: Router, private paymentsService: PaymentsService, private userService: UserService) {
        userService.hasRole("administrator").subscribe(
            resp =>this.isAdministrator = resp,
            error => this.isAdministrator = false
        );

        userService.hasRole("candidate").subscribe(
            resp => {
                this.isCandidate = resp;
                if (this.isCandidate) {
                    this.router.navigate(['Profile']);
                }
            },
            error => this.isCandidate = false
        );

        paymentsService.getBalance().subscribe(
            resp => this.balance = resp,
            error => this.balance = 0
        );
        
        paymentsService.pollBalance().subscribe(
            resp => this.balance = resp,
            error => this.balance = 0
        );
    }

    public isRouteActive(route) {
        return this.router.isRouteActive(this.router.generate(route));
    }
}
