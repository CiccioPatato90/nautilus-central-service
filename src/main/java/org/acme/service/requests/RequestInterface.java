package org.acme.service.requests;

import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.base.BaseRequest;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.response.requests.RequestListResponse;

import java.util.List;
import java.util.Map;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

public interface RequestInterface {
    List<? extends BaseRequest> getList(RequestFilter filter);
    RequestListResponse add(Class<? extends BaseRequest> request);
    BaseRequest findByRequestId(String id);
    String approveRequest(RequestCommand command);

    default void addFilter(Map<String, Object> queryMap, String field, String value, boolean useRegex) {
        if (!isNullOrEmpty(value)) {
            if (useRegex) {
                queryMap.put(field, Map.of("$regex", value, "$options", "i")); // Case-insensitive regex
            } else {
                queryMap.put(field, value);
            }
        }
    }
}
