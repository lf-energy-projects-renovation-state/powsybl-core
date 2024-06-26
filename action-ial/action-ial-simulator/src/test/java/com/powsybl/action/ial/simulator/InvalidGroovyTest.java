/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator;

import com.powsybl.action.ial.dsl.ActionDslException;
import com.powsybl.action.ial.dsl.ActionDslLoader;
import com.powsybl.iidm.network.Network;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidGroovyTest {

    @Test
    void invalidTwoTypes() {
        assertThrows(ActionDslException.class, () -> new ActionDslLoader(getDslFile("/invalid-rule-with-two-types-action.groovy")).load(Mockito.mock(Network.class)));
    }

    @Test
    void invalidMissingActions() {
        assertThrows(ActionDslException.class, () -> new ActionDslLoader(getDslFile("/invalid-rule-without-actions.groovy")).load(Mockito.mock(Network.class)));
    }

    private GroovyCodeSource getDslFile(String path) {
        return new GroovyCodeSource(new InputStreamReader(getClass().getResourceAsStream(path)), "test", GroovyShell.DEFAULT_CODE_BASE);
    }
}
