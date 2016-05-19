package api.handlers.base;

import api.contracts.base.BaseRequest;
import api.contracts.base.BaseResponse;
import api.contracts.base.ErrorCodes;
import api.contracts.base.ErrorDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseHandler<TRequest extends BaseRequest, TResponse extends BaseResponse> {

    protected final Logger logger = LogManager.getLogger(getClass().getName());

    public final TResponse handle(TRequest request) {
        try {
            ArrayList<ErrorDto> errors = validate(request);

            if (errors.isEmpty()) {
                return handleBase(request);
            } else {
                return handleErrors(errors);
            }
        } catch (Exception e) {
            logger.error(e);
            return handleException(e);
        }
    }

    private TResponse handleErrors(List<ErrorDto> errors) {
        TResponse response = createResponse();

        response.Errors = (ArrayList<ErrorDto>) errors;

        return response;
    }

    public abstract ArrayList<ErrorDto> validate(TRequest request);

    public abstract TResponse handleBase(TRequest request);

    public abstract TResponse createResponse();

    public TResponse handleException(Exception e) {

        return handleException(getMessage(e), ErrorCodes.GENERAL_ERROR);
    }

    public TResponse handleException(String errorMessage, ErrorCodes errorCode) {
        TResponse response = createResponse();

        response.Errors = new ArrayList<>();

        ErrorDto error = new ErrorDto(errorMessage, errorCode);

        response.Errors.add(error);

        return response;
    }

    private String getMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return String.format("Unknown error (%s)", e.getClass());
        } else {
            return message;
        }
    }
}
