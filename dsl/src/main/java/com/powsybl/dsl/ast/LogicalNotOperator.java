/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dsl.ast;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LogicalNotOperator extends AbstractUnaryOperatorNode {

    public LogicalNotOperator(ExpressionNode child) {
        super(child);
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitNotOperator(this, arg);
    }
}
