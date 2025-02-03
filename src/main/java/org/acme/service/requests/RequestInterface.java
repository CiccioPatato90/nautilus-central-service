package org.acme.service.requests;

import org.acme.dto.requests.RequestFilter;
import org.acme.model.requests.BaseRequest;
import org.acme.model.response.RequestListResponse;

import java.util.List;

public interface RequestInterface {
    List<? extends BaseRequest> getList(RequestFilter filter);
    RequestListResponse add(Class<? extends BaseRequest> filter);
}
