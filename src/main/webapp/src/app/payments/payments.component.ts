import {Component} from 'angular2/core';
import {RouterOutlet, RouteConfig} from 'angular2/router';
import {Tabs} from "./tabs/tabs.component.ts";
import {PendingPayments} from './payments-pending/payments-pending.component.ts';
import {PayPayment} from './payments-pay/payments-pay.component.ts';
import {HistoryPayments} from './payments-history/payments-history.component.ts';
import {SuccessPayment} from './payments-success/payments-success.component.ts';
import {FailedPayment} from './payments-failed/payments-failed.component.ts';

@Component({
    selector: 'payments',
    template: require('./payments.component.html'),
    styles: [require('./payments.component.scss')],
    providers: [],
    directives : [RouterOutlet, Tabs],
    pipes: []
})
@RouteConfig([
    {path: '/' , name: 'Pending', component: PendingPayments, useAsDefault: true},
    {path: '/:id', name: 'Pay', component: PayPayment},
    {path: '/history', name: 'History', component: HistoryPayments},
    {path: '/success', name: 'Success', component: SuccessPayment},
    {path: '/failed', name: 'Failed', component: FailedPayment},
])
export class PaymentsCentral {
    options: any;

    constructor() {
        this.options = [{
            name: 'Pending payments',
            link: 'Pending'
        }, {
            name: 'History payments',
            link: 'History'
        }];
    }
}
