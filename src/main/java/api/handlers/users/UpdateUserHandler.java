package api.handlers.users;

import api.business.entities.User;
import api.business.services.interfaces.IFormService;
import api.business.services.interfaces.ILoginService;
import api.business.services.interfaces.IUserService;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import api.contracts.users.UpdateUserRequest;
import api.contracts.users.UpdateUserResponse;
import api.handlers.base.BaseHandler;
import api.helpers.validator.Validator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.Subject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;

@Stateless
public class UpdateUserHandler extends BaseHandler<UpdateUserRequest, UpdateUserResponse> {
    @Inject
    private IUserService userService;
    @Inject
    private ILoginService loginService;
    @Inject
    private IFormService formService;
    @PersistenceContext
    private EntityManager em;

    @Override
    public ArrayList<ErrorDto> validate(UpdateUserRequest request) {
        ArrayList<ErrorDto> authErrors = new Validator().isAuthenticated().getErrors();
        if (!authErrors.isEmpty()) return authErrors;

        ArrayList<ErrorDto> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new ErrorDto("request is missing", ErrorCodes.VALIDATION_ERROR));
            return errors;
        }

        if (request.id <= 0) {
            errors.add(new ErrorDto("id is not valid", ErrorCodes.VALIDATION_ERROR));
            return errors;
        }

        Subject subject = SecurityUtils.getSubject();
        User current = userService.get();
        if (current.getId() != request.id && !subject.hasRole("administrator")) {
            errors.add(new ErrorDto("can not edit other users", ErrorCodes.ACCESS_DENIED));
            return errors;
        }

        User user = userService.get(request.id);
        if (user == null) {
            errors.add(new ErrorDto("User does not exist", ErrorCodes.NOT_FOUND));
        }

        if (request.name != null && request.name.length() < 1) {
            errors.add(new ErrorDto("Name must be provided", ErrorCodes.VALIDATION_ERROR));
        }

        if (request.password != null && request.password.length() < 6) {
            errors.add(new ErrorDto("Password must be at least 6 characters length", ErrorCodes.VALIDATION_ERROR));
        }

        if (request.password != null && request.passwordConfirm != null && !request.password.equals(request.passwordConfirm)) {
            errors.add(new ErrorDto("Passwords does not match", ErrorCodes.VALIDATION_ERROR));
        }

        if (request.email != null && request.email.length() < 5) {
            errors.add(new ErrorDto("Email must be provided", ErrorCodes.VALIDATION_ERROR));
        }

        if (!current.equals(user)) {
            errors.add(new ErrorDto("Email already taken", ErrorCodes.DUPLICATE_EMAIL));
        }
        errors.addAll(formService.validateFormFields(request.fields));

        return errors;
    }

    @Override
    public UpdateUserResponse handleBase(UpdateUserRequest request) {
        UpdateUserResponse response = createResponse();

        User user = userService.get(request.id);

        if (request.fields != null)
            formService.saveFormResults(request.fields, user);

        if (request.name != null) {
            user.setName(request.name);
        }

        if (request.email != null) {
            user.getLogin().setEmail(request.email);
        }

        if (request.picture != null) {
            user.setPicture(request.picture);
        }

        if (request.password != null) {
            PasswordService passwordService = new DefaultPasswordService();
            String encryptedPassword = passwordService.encryptPassword(request.password);

            user.getLogin().setPassword(encryptedPassword);
        }

        return response;
    }

    @Override
    public UpdateUserResponse createResponse() {
        return new UpdateUserResponse();
    }
}
