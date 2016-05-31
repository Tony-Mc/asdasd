package api.handlers.cottages;

import api.business.entities.Cottage;
import api.business.persistance.ISimpleEntityManager;
import api.business.services.interfaces.ICottageService;
import api.contracts.cottages.GetCottagesResponse;
import api.contracts.cottages.GetCottagesRequest;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import api.contracts.dto.CottageDto;
import api.handlers.base.BaseHandler;
import api.helpers.validator.Validator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class GetCottagesHandler extends BaseHandler<GetCottagesRequest, GetCottagesResponse> {
    @Inject
    private ICottageService cottageService;
    @Inject
    private ISimpleEntityManager sem;

    @Override
    public ArrayList<ErrorDto> validate(GetCottagesRequest request) {
        ArrayList<ErrorDto> errors = new Validator().isMember().getErrors();

        if (request == null) {
            errors.add(new ErrorDto("Request must be provided.", ErrorCodes.VALIDATION_ERROR));
        }

        return errors;
    }

    @Override
    public GetCottagesResponse handleBase(GetCottagesRequest request) {
        GetCottagesResponse response = createResponse();

        List<Cottage> allCottages;
        if (withoutFilters(request)) {
            allCottages = sem.getAll(Cottage.class);
        } else {
            allCottages = cottageService.getByFilters(request.title, request.bedcount, request.dateFrom, request.dateTo, request.priceFrom, request.priceTo);
        }

        response.cottages = allCottages.stream().map(CottageDto::new).collect(Collectors.toList());

        return response;
    }

    private boolean withoutFilters(GetCottagesRequest request) {
        return request.title == null &&
                request.dateFrom == null &&
                request.dateTo == null &&
                request.bedcount == 0 &&
                request.priceFrom == 0 &&
                request.priceTo == 0;
    }

    @Override
    public GetCottagesResponse createResponse() {
        return new GetCottagesResponse();
    }
}
