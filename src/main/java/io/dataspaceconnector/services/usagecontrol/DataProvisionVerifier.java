/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.services.usagecontrol;

import de.fraunhofer.iais.eis.ContractAgreement;
import io.dataspaceconnector.config.ConnectorConfiguration;
import io.dataspaceconnector.exceptions.PolicyRestrictionException;
import io.dataspaceconnector.utils.ContractUtils;
import io.dataspaceconnector.utils.RuleUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
@Log4j2
@RequiredArgsConstructor
public class DataProvisionVerifier implements PolicyVerifier<VerificationInput> {

    /**
     * The policy execution point.
     */
    private final @NonNull RuleValidator ruleValidator;

    /**
     * Service for configuring policy settings.
     */
    private final @NonNull ConnectorConfiguration connectorConfig;

    /**
     * Policy check on data provision on provider side.
     *
     * @param target          The requested element.
     * @param issuerConnector The issuer connector.
     * @param agreement       The ids contract agreement.
     * @throws PolicyRestrictionException If a policy restriction has been detected.
     */
    public void checkPolicy(final URI target,
                            final URI issuerConnector,
                            final ContractAgreement agreement) throws PolicyRestrictionException {
        final var patternsToCheck = Arrays.asList(
                PolicyPattern.PROVIDE_ACCESS,
                PolicyPattern.PROHIBIT_ACCESS,
                PolicyPattern.USAGE_DURING_INTERVAL,
                PolicyPattern.USAGE_UNTIL_DELETION,
                PolicyPattern.CONNECTOR_RESTRICTED_USAGE);
        try {
            checkForAccess(patternsToCheck, target, issuerConnector, agreement);
        } catch (PolicyRestrictionException exception) {
            // Unknown patterns cause an exception. Ignore if unsupported patterns are allowed.
            if (!connectorConfig.isAllowUnsupported()) {
                throw exception;
            }
        }
    }

    /**
     * Checks the contract content for data access (on provider side).
     *
     * @param patterns        List of patterns that should be enforced.
     * @param target          The requested element.
     * @param issuerConnector The issuer connector.
     * @param agreement       The ids contract agreement.
     * @throws PolicyRestrictionException If a policy restriction has been detected.
     */
    public void checkForAccess(final List<PolicyPattern> patterns,
                               final URI target, final URI issuerConnector,
                               final ContractAgreement agreement)
            throws PolicyRestrictionException {
        final var rules = ContractUtils.getRulesForTargetId(agreement, target);

        // Check the policy of each rule.
        for (final var rule : rules) {
            final var pattern = RuleUtils.getPatternByRule(rule);
            // Enforce only a set of patterns.
            if (patterns.contains(pattern)) {
                ruleValidator.validatePolicy(pattern, rule, target, issuerConnector);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VerificationResult verify(final VerificationInput input) {
        try {
            this.checkPolicy(input.getTarget(), input.getIssuerConnector(), input.getAgreement());
            return VerificationResult.ALLOWED;
        } catch (PolicyRestrictionException exception) {
            if (log.isDebugEnabled()) {
                log.debug("Data access denied. [input=({})]", input, exception);
            }
            return VerificationResult.DENIED;
        }
    }
}