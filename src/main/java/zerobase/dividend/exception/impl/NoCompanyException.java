package zerobase.dividend.exception.impl;

import org.springframework.http.HttpStatus;
import zerobase.dividend.exception.AbstractException;

public class NoCompanyException extends AbstractException {

    @Override
    public int getStatusCode(){
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage(){
        return "Company does not exists.";
    }
}
