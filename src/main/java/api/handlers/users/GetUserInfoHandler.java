package api.handlers.users;

import api.business.entities.User;
import api.business.services.interfaces.ILoginService;
import api.business.services.interfaces.IUserService;
import api.contracts.users.GetUserInfoRequest;
import api.contracts.users.GetUserInfoResponse;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import api.handlers.base.BaseHandler;
import api.helpers.Validator;
import clients.facebook.interfaces.IFacebookClient;
import clients.facebook.responses.FacebookUserDetails;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@Stateless
public class GetUserInfoHandler extends BaseHandler<GetUserInfoRequest, GetUserInfoResponse> {

    @Inject
    private IUserService userInfoService;
    @Inject
    private ILoginService loginService;
    @Inject
    private IFacebookClient facebookClient;

    @Override
    public ArrayList<ErrorDto> validate(GetUserInfoRequest request) {

        ArrayList<ErrorDto> errors = Validator.checkAllNotNullAndIsAuthenticated(request);

        return errors;
    }

    @Override
    public GetUserInfoResponse handleBase(GetUserInfoRequest request) {
        Subject currentUser = SecurityUtils.getSubject();

        GetUserInfoResponse response = createResponse();
        String username = currentUser.getPrincipal().toString();

        User user = loginService.getByUserName(username).getUser();
        response.Email = user.getEmail();
        if (user == null) {
            logger.warn(String.format("User %s not found", username));
            return handleException("User not found", ErrorCodes.NOT_FOUND);
        }

        if (user.isFacebookUser()) {
            try {
                FacebookUserDetails userDetails = facebookClient.getMyDetails();
                if (!userDetails.Picture.isSilhouette()) {
                    response.Picture = userDetails.Picture.getUrl();
                }
            } catch (IOException e) {
                handleException(e);
            }
        }

        response.Name = user.getName();

        return response;
    }

    @Override
    public GetUserInfoResponse createResponse() {
        return new GetUserInfoResponse();
    }
}
