/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import com.powsybl.iidm.network.TwoSides;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.Test;

import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolationsResult;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LimitViolationsResultEquivalenceTest {

    @Test
    void equivalent() {
        LimitViolationsResultEquivalence resultEquivalence = new LimitViolationsResultEquivalence(0.1, NullWriter.INSTANCE);

        LimitViolation line1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);
        LimitViolation sameLine1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.ONE);
        LimitViolation line1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.TWO);
        LimitViolation sameLine1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, TwoSides.TWO);
        LimitViolation similarLine1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.09, TwoSides.TWO);
        LimitViolation differentLine1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1101.0, TwoSides.TWO);
        LimitViolation line2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 900.0, 0.95f, 950.0, TwoSides.ONE);
        LimitViolation sameLine2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 900.0, 0.95f, 950.0, TwoSides.ONE);
        LimitViolation smallLine2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 900.0, 1, 900.09, TwoSides.TWO);
        LimitViolation vl1Violation1 = new LimitViolation("VL1", LimitViolationType.HIGH_VOLTAGE, 200.0, 1, 250.0);
        LimitViolation sameVl1Violation1 = new LimitViolation("VL1", LimitViolationType.HIGH_VOLTAGE, 200.0, 1, 250.0);
        LimitViolation vl1Violation2 = new LimitViolation("VL1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 200.0, 1, 250.0);
        LimitViolation sameVl1Violation2 = new LimitViolation("VL1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 200.0, 1, 250.0);
        LimitViolation smallVl2Violation = new LimitViolation("VL2", LimitViolationType.HIGH_VOLTAGE, 200.0, 1, 200.09);

        // computation ko in both results
        LimitViolationsResult result1 = new LimitViolationsResult(Collections.emptyList(),
                                                                  Collections.emptyList());
        LimitViolationsResult result2 = new LimitViolationsResult(Collections.emptyList(),
                                                                  Collections.emptyList());
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // different computation results
        result1 = new LimitViolationsResult(Arrays.asList(line1Violation2, vl1Violation1, line2Violation, line1Violation1, vl1Violation2),
                                            Arrays.asList("action1", "action2"));
        assertFalse(resultEquivalence.equivalent(result1, result2));

        // identical results
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, sameLine1Violation2, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2", "action1"));
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // similar results
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, similarLine1Violation2, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2", "action1"));
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // different violations
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, differentLine1Violation2, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2", "action1"));
        assertFalse(resultEquivalence.equivalent(result1, result2));

        // more violations in result1
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2", "action1"));
        assertFalse(resultEquivalence.equivalent(result1, result2));

        // more violations, but small, in result2
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, sameLine1Violation2, sameLine1Violation1, sameVl1Violation2, smallLine2Violation),
                                            Arrays.asList("action2", "action1"));
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // more violations in result2
        result1 = new LimitViolationsResult(Arrays.asList(line1Violation2, line2Violation, line1Violation1, vl1Violation2),
                                            Arrays.asList("action1", "action2"));
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, sameLine1Violation2, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2", "action1"));
        assertFalse(resultEquivalence.equivalent(result1, result2));

        // more violations, but small, in result1
        result1 = new LimitViolationsResult(Arrays.asList(smallLine2Violation, line1Violation2, vl1Violation1, line2Violation, line1Violation1, vl1Violation2),
                                            Arrays.asList("action1", "action2"));
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, sameLine1Violation2, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2", "action1"));
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // more violations, but small, at the end of (sorted) result1
        result1 = new LimitViolationsResult(Arrays.asList(similarLine1Violation2, vl1Violation1, line2Violation, smallVl2Violation, line1Violation1, vl1Violation2),
                                            Arrays.asList("action1", "action2"));
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // more violations, but small, at the end of (sorted) result2
        result1 = new LimitViolationsResult(Arrays.asList(line1Violation2, vl1Violation1, line2Violation, line1Violation1, vl1Violation2),
                                            Arrays.asList("action1", "action2"));
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, similarLine1Violation2, sameLine1Violation1, sameVl1Violation2, smallVl2Violation),
                                            Arrays.asList("action2", "action1"));
        assertTrue(resultEquivalence.equivalent(result1, result2));

        // different actions
        result1 = new LimitViolationsResult(Arrays.asList(line1Violation2, vl1Violation1, line2Violation, line1Violation1, vl1Violation2),
                                            Arrays.asList("action1", "action2"));
        result2 = new LimitViolationsResult(Arrays.asList(sameLine2Violation, sameVl1Violation1, sameLine1Violation2, sameLine1Violation1, sameVl1Violation2),
                                            Arrays.asList("action2"));
        assertFalse(resultEquivalence.equivalent(result1, result2));
    }

}
