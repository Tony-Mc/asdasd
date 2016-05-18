package api.handlers.Recommendation;

import api.business.services.interfaces.IRecommendationService;
import api.contracts.recommendations.SendRecommendationRequest;
import api.contracts.recommendations.SendRecommendationResponse;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import api.handlers.base.BaseHandler;
import api.helpers.Validator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.util.ArrayList;

@Stateless
public class SendRecommendationHandler extends BaseHandler<SendRecommendationRequest, SendRecommendationResponse> {
    @Inject
    private IRecommendationService recommendationService;

    @Override
    public ArrayList<ErrorDto> validate(SendRecommendationRequest request) {
        ArrayList<ErrorDto> errors = new ArrayList<>();

        if (!SecurityUtils.getSubject().isAuthenticated()) {
            errors.add(new ErrorDto("Not authenticated.", ErrorCodes.AUTHENTICATION_ERROR));
            return errors;
        }
        if (recommendationService.isRequestLimitReached()) {
            errors.add(new ErrorDto("Request limit reached", ErrorCodes.VALIDATION_ERROR));
        }

        return errors;
    }

    @Override
    public SendRecommendationResponse handleBase(SendRecommendationRequest request) {

        SendRecommendationResponse response = createResponse();
        try {
            recommendationService.sendRecommendationRequest(request.UserEmail);
        } catch (Exception e) {
            Throwable er = ExceptionUtils.getRootCause(e);
            if (er instanceof BadRequestException)
                response = handleException(er.getMessage(), ErrorCodes.BAD_REQUEST);
            else
                return handleException(e);
        }
        return response;
    }

    @Override
    public SendRecommendationResponse createResponse() {
        return new SendRecommendationResponse();
    }
}
