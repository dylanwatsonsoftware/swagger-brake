package io.redskap.swagger.brake.core.rule.request;

import static java.util.function.Function.identity;

import java.util.*;
import java.util.stream.Collectors;

import io.redskap.swagger.brake.core.model.*;
import io.redskap.swagger.brake.core.rule.BreakingChangeRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestTypeChangedRule implements BreakingChangeRule<RequestTypeChangedBreakingChange> {
    @Override
    public Collection<RequestTypeChangedBreakingChange> checkRule(Specification oldApi, Specification newApi) {
        Set<RequestTypeChangedBreakingChange> breakingChanges = new HashSet<>();
        for (Path path : oldApi.getPaths()) {
            if (path.isBetaApi()) {
                log.debug("Skipping {} as it's marked as a beta API", path);
                continue;
            }
            Optional<Path> newApiPath = newApi.getPath(path);
            if (newApiPath.isPresent()) {
                Path newPath = newApiPath.get();
                Optional<Request> requestBody = path.getRequestBody();
                Optional<Request> newRequestBody = newPath.getRequestBody();
                if (requestBody.isPresent() && newRequestBody.isPresent()) {
                    for (Map.Entry<MediaType, Schema> entry : requestBody.get().getMediaTypes().entrySet()) {
                        MediaType mediaType = entry.getKey();
                        Schema schema = entry.getValue();
                        Optional<Schema> newApiSchema = newRequestBody.get().getSchemaByMediaType(mediaType);
                        if (newApiSchema.isPresent()) {
                            Schema newSchema = newApiSchema.get();
                            Collection<SchemaAttribute> oldSchemaAttributes = schema.getSchemaAttributes();
                            if (CollectionUtils.isNotEmpty(oldSchemaAttributes)) {
                                Map<String, SchemaAttribute> newSchemaAttributesMap =
                                    newSchema.getSchemaAttributes().stream().collect(Collectors.toMap(SchemaAttribute::getName, identity()));
                                for (SchemaAttribute oldSchemaAttribute : oldSchemaAttributes) {
                                    String oldAttributeName = oldSchemaAttribute.getName();
                                    String oldAttributeType = oldSchemaAttribute.getSchema().getType();
                                    SchemaAttribute newSchemaAttribute = newSchemaAttributesMap.get(oldAttributeName);
                                    if (newSchemaAttribute != null) {
                                        String newAttributeType = newSchemaAttribute.getSchema().getType();
                                        if (!oldAttributeType.equalsIgnoreCase(newAttributeType)) {
                                            breakingChanges.add(new RequestTypeChangedBreakingChange(path.getPath(), path.getMethod(), oldAttributeName,
                                                oldAttributeType, newAttributeType));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return breakingChanges;
    }
}
