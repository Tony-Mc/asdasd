package api.handlers.payments;

import api.business.entities.MoneyTransaction;
import api.business.entities.Payment;
import api.business.entities.User;
import api.business.services.interfaces.IPaymentsService;
import api.business.services.interfaces.IUserService;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import api.contracts.constants.Currency;
import api.contracts.enums.PaymentTypes;
import api.contracts.enums.TransactionStatus;
import api.contracts.enums.TransactionTypes;
import api.contracts.payments.PayClubbyRequest;
import api.contracts.payments.PayClubbyResponse;
import api.handlers.base.BaseHandler;
import api.helpers.validator.Validator;
import logging.audit.Audit;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@Stateless
public class PayClubbyHandler extends BaseHandler<PayClubbyRequest, PayClubbyResponse> {

    @Inject
    private IPaymentsService paymentsService;
    @Inject
    private IUserService userService;

    @Override
    public ArrayList<ErrorDto> validate(PayClubbyRequest request) {
        ArrayList<ErrorDto> authErrors = new Validator().isMember().getErrors();

        if (!authErrors.isEmpty())
            return authErrors;

        return new Validator().allFieldsSet(request).getErrors();
    }

    @Override
    @Audit
    public PayClubbyResponse handleBase(PayClubbyRequest request) {
        PayClubbyResponse response = createResponse();
        Payment payment = paymentsService.getPayment(request.PaymentId);

        if (payment == null) {
            response.Errors = new ArrayList<>();
            response.Errors.add(new ErrorDto(String.format("Payment %s not found", request.PaymentId), ErrorCodes.NOT_FOUND));
            return response;
        }

        if (payment.getPaymenttypeid() != PaymentTypes.pay.getValue()) {
            response.Errors = new ArrayList<>();
            response.Errors.add(new ErrorDto(String.format("Payment %s cannot be paid with Clubby coins", request.PaymentId), ErrorCodes.VALIDATION_ERROR));
            return response;
        }

        User user = userService.get();

        if (!payment.canAcces(user)) {
            response.Errors = new ArrayList<>();
            response.Errors.add(new ErrorDto("Cant access this payment", ErrorCodes.VALIDATION_ERROR));
            return response;
        }

        int balance = paymentsService.getMyBalance(user.getId());

        if (balance < payment.calculatePrice()) {
            response.Errors = new ArrayList<>();
            response.Errors.add(new ErrorDto(String.format("Not enough clubby money. Balance %s", balance / 100d), ErrorCodes.LOW_BALANCE));
            return response;
        }

        MoneyTransaction mt = new MoneyTransaction();
        mt.setStatus(TransactionStatus.approved.getValue());
        mt.setPayment(payment);
        mt.setUser(user);
        mt.setTransactionid(UUID.randomUUID().toString());
        mt.setCreationTime(new Date());
        mt.setTransactionTypeId(TransactionTypes.clubby.getValue());
        mt.setAmount(payment.calculatePrice());
        mt.setCurrency(Currency.ClubbyCoin);

        paymentsService.createMoneyTransaction(mt);

        response.success = true;

        return response;
    }

    @Override
    public PayClubbyResponse createResponse() {
        return new PayClubbyResponse();
    }
}
