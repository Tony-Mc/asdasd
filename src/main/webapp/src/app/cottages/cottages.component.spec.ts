import {
    it,
    iit,
    describe,
    ddescribe,
    expect,
    inject,
    injectAsync,
    TestComponentBuilder,
    beforeEachProviders
} from 'angular2/testing';
import {provide} from 'angular2/core';
import {Cottages} from './cottages.component';


describe('CottagesHome Component', () => {

    beforeEachProviders((): any[] => []);

    it('should ...', injectAsync([TestComponentBuilder], (tcb: TestComponentBuilder) => {
        return tcb.createAsync(Cottages).then((fixture) => {
            fixture.detectChanges();
        });
    }));
});
