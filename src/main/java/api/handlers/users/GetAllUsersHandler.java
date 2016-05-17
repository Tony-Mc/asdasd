package api.handlers.users;

import api.business.entities.User;
import api.business.persistance.ISimpleEntityManager;
import api.contracts.base.ErrorDto;
import api.contracts.dto.UserDto;
import api.contracts.users.GetAllUsersRequest;
import api.contracts.users.GetAllUsersResponse;
import api.handlers.base.BaseHandler;
import api.helpers.Validator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Stateless
public class GetAllUsersHandler extends BaseHandler<GetAllUsersRequest, GetAllUsersResponse> {
    @Inject
    private ISimpleEntityManager entityManager;

    @Override
    public ArrayList<ErrorDto> validate(GetAllUsersRequest request) {
        return Validator.checkAllNotNullAndIsAuthenticated(request);
    }

    @Override
    public GetAllUsersResponse handleBase(GetAllUsersRequest request) {
        GetAllUsersResponse response = createResponse();

        response.users = entityManager.getAll(User.class).stream().map(UserDto::new).collect(Collectors.toList());

        return response;
    }

    @Override
    public GetAllUsersResponse createResponse() {
        return new GetAllUsersResponse();
    }
}
