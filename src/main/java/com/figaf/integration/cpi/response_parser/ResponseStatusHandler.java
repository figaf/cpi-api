package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.exception.ClientIntegrationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseStatusHandler {

    public static void handleResponseStatus(ResponseEntity<?> responseEntity, String operation) {
        switch (responseEntity.getStatusCode().value()) {
            case 200:
            case 201:
            case 202: {
                log.debug("operation {} was successful, responseBody {}", operation, responseEntity.getBody());
                break;
            }
            default: {
                throw new ClientIntegrationException(String.format(
                        "operation %s failed , Code: %d, Message: %s",
                        operation,
                        responseEntity.getStatusCode().value(),
                        responseEntity.getBody())
                );
            }
        }
    }
}
