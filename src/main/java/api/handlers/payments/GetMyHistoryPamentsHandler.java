package api.handlers.payments;

import api.business.entities.MoneyTransaction;
import api.business.entities.Payment;
import api.business.entities.User;
import api.business.services.interfaces.IPaymentsService;
import api.business.services.interfaces.IUserService;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import api.contracts.dto.MoneyTransactionDto;
import api.contracts.payments.GetMyHistoryPaymetsRequest;
import api.contracts.payments.GetMyHistoryPaymetsResponse;
import api.handlers.base.BaseHandler;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class GetMyHistoryPamentsHandler extends BaseHandler<GetMyHistoryPaymetsRequest, GetMyHistoryPaymetsResponse> {
    @Inject
    private IPaymentsService paymentsService;
    @Inject
    private IUserService userService;

    @Override
    public ArrayList<ErrorDto> validate(GetMyHistoryPaymetsRequest request) {
        ArrayList<ErrorDto> errors =  new ArrayList<>();
        Subject currentUser = SecurityUtils.getSubject();

        if (!currentUser.isAuthenticated()) {
            errors.add(new ErrorDto("Not authenticated.", ErrorCodes.AUTHENTICATION_ERROR));
        }

        return errors;
    }

    @Override
    public GetMyHistoryPaymetsResponse handleBase(GetMyHistoryPaymetsRequest request) {
        GetMyHistoryPaymetsResponse response = createResponse();
        Subject currentUser = SecurityUtils.getSubject();

        String username = currentUser.getPrincipal().toString();
        User user = userService.getByUsername(username);

        List<MoneyTransaction> payments = paymentsService.getMoneyTransactionsByUserId(user.getId());

        response.payments =  payments.stream().map(MoneyTransactionDto::new).collect(Collectors.toList());

        return response;
    }

    @Override
    public GetMyHistoryPaymetsResponse createResponse() {
        return new GetMyHistoryPaymetsResponse();
    }
}
