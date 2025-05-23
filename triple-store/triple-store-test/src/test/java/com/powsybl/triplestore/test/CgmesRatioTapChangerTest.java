/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.test.TripleStoreTester.Expected;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesRatioTapChangerTest {

    @BeforeAll
    static void setUp() {
        queries = new QueryCatalog("cgmes-rtcs/cgmes-rtcs.sparql");
        String base = "foo:cgmes-rtcs";
        String[] inputs = {"cgmes-rtcs/rtc-EQ.xml", "cgmes-rtcs/rtc-SSH.xml"};
        tester = new TripleStoreTester(TripleStoreFactory.allImplementations(), base, inputs);
        tester.load();
    }

    @Test
    void testTapChangerControls() {
        Expected expected = new Expected().expect("TapChangerControl",
                "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b", "_97110e84-7da6-479c-846c-696fdaa83d56");
        tester.testQuery(queries.get("tapChangerControls"), expected);
    }

    @Test
    void testTapChangerControlsOptionalSsh() {
        Expected expected = new Expected().expect("TapChangerControl",
                "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b", "_97110e84-7da6-479c-846c-696fdaa83d56");
        tester.testQuery(queries.get("tapChangerControlsOptionalSSH"), expected);
    }

    @Test
    void testTapChangerControlsOnlySshData() {
        Expected expected = new Expected().expect("TapChangerControl",
                "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b", "_38f972bc-b7fd-4e75-8c24-379a86fbb506");
        tester.testQuery(queries.get("tapChangerControlsOnlySSHData"), expected);
    }

    @Test
    void testTapChangerOptionalControlOptionalSsh() {
        Expected expected = new Expected()
                .expect(
                        "RatioTapChanger",
                        "_11111111-4a10-4031-b008-60c0dc340a07",
                        "_955d9cd0-4a10-4031-b008-60c0dc340a07",
                        "_83cc66dd-8d93-4a2c-8103-f1f5a9cf7e2e",
                        "_fe25f43a-7341-446e-a71a-8ab7119ba806")
                .expect(
                        "TapChangerControl",
                        null,
                        "_ee42c6c2-39e7-43c2-9bdd-d397c5dc980b",
                        "_97110e84-7da6-479c-846c-696fdaa83d56",
                        null);
        tester.testQuery(queries.get("tapChangerOptionalControlOptionalSSH"), expected);
    }

    private static TripleStoreTester tester;
    private static QueryCatalog queries;
}
