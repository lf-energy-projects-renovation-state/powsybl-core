/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Overload;

import java.util.Objects;

/**
 * A simple, default implementation of {@link Overload}.
 *
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class OverloadImpl implements Overload {

    private final LoadingLimits.TemporaryLimit temporaryLimit;

    private final String previousLimitName;

    private final double previousLimit;
    private final double limitReductionCoefficient;

    public OverloadImpl(LoadingLimits.TemporaryLimit temporaryLimit, String previousLimitName, double previousLimit) {
        this(temporaryLimit, previousLimitName, previousLimit, 1);
    }

    public OverloadImpl(LoadingLimits.TemporaryLimit temporaryLimit, String previousLimitName, double previousLimit, double limitReductionCoefficient) {
        this.temporaryLimit = Objects.requireNonNull(temporaryLimit);
        this.previousLimitName = previousLimitName;
        this.previousLimit = previousLimit;
        this.limitReductionCoefficient = limitReductionCoefficient;
    }

    @Override
    public LoadingLimits.TemporaryLimit getTemporaryLimit() {
        return temporaryLimit;
    }

    @Override
    public String getPreviousLimitName() {
        return previousLimitName;
    }

    @Override
    public double getPreviousLimit() {
        return previousLimit;
    }

    @Override
    public double getLimitReductionCoefficient() {
        return limitReductionCoefficient;
    }
}
