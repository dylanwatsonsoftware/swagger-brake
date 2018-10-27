package com.arnoldgalovics.blog.swagger.breaker.core;

import static java.util.stream.Collectors.toList;

import java.util.Collection;

import com.arnoldgalovics.blog.swagger.breaker.core.model.Specification;
import com.arnoldgalovics.blog.swagger.breaker.core.rule.BreakingChangeRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultBreakChecker implements BreakChecker {
    private final Collection<BreakingChangeRule<? extends BreakingChange>> rules;

    @Override
    public Collection<BreakingChange> check(Specification oldApi, Specification newApi) {
        if (log.isDebugEnabled()) {
            rules.stream().map(BreakingChangeRule::getClass).map(Class::getName).forEach(name -> log.debug("Rule configured: {}", name));
        }
        if (oldApi == null) {
            throw new IllegalArgumentException("oldApi must be provided");
        }
        if (newApi == null) {
            throw new IllegalArgumentException("newApi must be provided");
        }
        return rules.stream().map(rule -> rule.checkRule(oldApi, newApi)).flatMap(Collection::stream).collect(toList());
    }
}
