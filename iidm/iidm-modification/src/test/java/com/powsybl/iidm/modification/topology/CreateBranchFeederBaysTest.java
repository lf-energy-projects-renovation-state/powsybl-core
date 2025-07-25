/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionImpl;
import com.powsybl.iidm.network.impl.extensions.ConnectablePositionImpl;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsAfter;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getUnusedOrderPositionsBefore;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CreateBranchFeederBaysTest extends AbstractModificationTest {

    private final Network bbNetwork = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
    private final Network nbNetwork = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

    @Test
    void baseNodeBreakerLineTest() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(85)
                .withDirection1(BOTTOM)
                .withFeederName1("lineTestFeeder1")
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(75)
                .withFeederName2("lineTestFeeder2")
                .withDirection2(TOP)
                .build();
        modification.apply(nbNetwork);
        writeXmlTest(nbNetwork, "/network-node-breaker-with-new-line.xml");
    }

    @Test
    void baseBusBreakerLineTest() throws IOException {
        LineAdder lineAdder = bbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("NGEN")
                .withPositionOrder1(85)
                .withDirection1(BOTTOM)
                .withFeederName1("lineTestFeeder1")
                .withBusOrBusbarSectionId2("NHV1")
                .withFeederName2("lineTestFeeder2")
                .withDirection2(TOP)
                .build();
        modification.apply(bbNetwork);
        writeXmlTest(bbNetwork, "/eurostag-create-line-feeder-bays.xml");
    }

    @Test
    void usedOrderLineTest() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs1")
                .withPositionOrder1(70)
                .withDirection1(TOP)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(85)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);
        writeXmlTest(nbNetwork, "/network-node-breaker-with-new-line-order-used.xml");
    }

    @Test
    void baseInternalLineTest() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs2")
                .withPositionOrder1(105)
                .withDirection1(TOP)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(14)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);
        writeXmlTest(nbNetwork, "/network-node-breaker-with-new-internal-line.xml");
    }

    @Test
    void getUnusedOrderPositionAfter() {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        Optional<Range<Integer>> unusedOrderPositionsAfter = getUnusedOrderPositionsAfter(nbNetwork.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(121, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        int positionOrder1 = unusedOrderPositionsAfter.get().getMinimum();
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs2")
                .withPositionOrder1(positionOrder1)
                .withDirection1(TOP)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);

        ConnectablePosition<Line> newLine = nbNetwork.getLine("lineTest").getExtension(ConnectablePosition.class);
        assertEquals(TOP, newLine.getFeeder1().getDirection());
        assertEquals(Optional.of(121), newLine.getFeeder1().getOrder());
    }

    @Test
    void getUnusedOrderPositionBefore() {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        Optional<Range<Integer>> unusedOrderPositionsBefore = getUnusedOrderPositionsBefore(nbNetwork.getBusbarSection("bbs2"));
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(71, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(79, (int) unusedOrderPositionsBefore.get().getMaximum());
        int positionOrder1 = unusedOrderPositionsBefore.get().getMaximum();

        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs2")
                .withPositionOrder2(positionOrder1)
                .withDirection2(BOTTOM)
                .build();
        modification.apply(nbNetwork);

        ConnectablePosition<Line> newLine = nbNetwork.getLine("lineTest").getExtension(ConnectablePosition.class);
        assertEquals(BOTTOM, newLine.getFeeder2().getDirection());
        assertEquals(Optional.of(79), newLine.getFeeder2().getOrder());
    }

    @Test
    void testException() {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);

        //wrong network
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestWrongNetwork")
                .build();
        Network network1 = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        CreateBranchFeederBays modification0 = new CreateBranchFeederBaysBuilder().
                withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs1")
                .withPositionOrder1(11)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(11)
                .withDirection2(BOTTOM)
                .build();
        assertDoesNotThrow(() -> modification0.apply(network1, false, ReportNode.NO_OP));
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> modification0.apply(network1, true, reportNode1));
        assertEquals("Network given in parameters and in connectableAdder are different. Connectable lineTest of type LINE was added then removed", e0.getMessage());
        assertEquals("core.iidm.modification.networkMismatch", reportNode1.getChildren().get(0).getMessageKey());

        // not found id
        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestUndefinedId")
                .build();
        CreateBranchFeederBays modification1 = new CreateBranchFeederBaysBuilder().
                withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        assertDoesNotThrow(() -> modification1.apply(nbNetwork, false, ReportNode.NO_OP));
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modification1.apply(nbNetwork, true, reportNode2));
        assertEquals("Bus or busbar section bbs not found", e1.getMessage());
        assertEquals("core.iidm.modification.notFoundBusOrBusbarSection", reportNode2.getChildren().get(0).getMessageKey());

        // wrong identifiable type
        ReportNode reportNode3 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestWrongBbsType")
                .build();
        CreateBranchFeederBays modification2 = new CreateBranchFeederBaysBuilder().
                withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("gen1")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs5")
                .withPositionOrder2(115)
                .withDirection2(BOTTOM)
                .build();
        assertDoesNotThrow(() -> modification2.apply(nbNetwork, false, ReportNode.NO_OP));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification2.apply(nbNetwork, true, reportNode3));
        assertEquals("Unsupported type GENERATOR for identifiable gen1", e2.getMessage());
        assertEquals("core.iidm.modification.unsupportedIdentifiableType", reportNode3.getChildren().get(0).getMessageKey());
    }

    @Test
    void baseNodeBreakerTwoWindingsTransformerTest() throws IOException {
        TwoWindingsTransformerAdder twtAdder = nbNetwork.getSubstation("subst")
                .newTwoWindingsTransformer()
                .setId("twtTest")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(220.0)
                .setRatedU2(400.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(twtAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(71)
                .withDirection2(TOP)
                .build();
        modification.apply(nbNetwork);
        writeXmlTest(nbNetwork, "/network-node-breaker-with-new-twt-feeders-bbs.xml");
    }

    @Test
    void baseBusBreakerTwoWindingsTransformerTest() throws IOException {
        TwoWindingsTransformerAdder twtAdder = bbNetwork.getSubstation("P1")
                .newTwoWindingsTransformer()
                .setId("twtTest")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(220.0)
                .setRatedU2(400.0);
        NetworkModification modification = new CreateBranchFeederBaysBuilder()
                .withBranchAdder(twtAdder)
                .withBusOrBusbarSectionId1("NGEN")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("NHV1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build();
        modification.apply(bbNetwork);
        writeXmlTest(bbNetwork, "/eurostag-create-twt-feeder-bays.xml");
    }

    @Test
    void testWithoutExtension() {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build()
                .apply(network, true, ReportNode.NO_OP);

        Line line = network.getLine("lineTest");
        assertNotNull(line);

        ConnectablePosition<Line> position = line.getExtension(ConnectablePosition.class);
        assertNull(position);
    }

    @Test
    void testWithReportNode() throws IOException {
        LineAdder lineAdder = nbNetwork.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateLine")
                .build();
        new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(85)
                .withDirection1(BOTTOM)
                .withFeederName1("lineTestFeeder1")
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(75)
                .withFeederName2("lineTestFeeder2")
                .withDirection2(TOP)
                .build().apply(nbNetwork, true, reportNode);
        testReportNode(reportNode, "/reportNode/create-line-NB-report.txt");
    }

    @Test
    void testReportNodeWithoutExtension() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LineAdder lineAdder = network.newLine()
                .setId("lineTest")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0);
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateLineWithoutExtensions")
                .build();
        new CreateBranchFeederBaysBuilder()
                .withBranchAdder(lineAdder)
                .withBusOrBusbarSectionId1("bbs5")
                .withPositionOrder1(115)
                .withDirection1(BOTTOM)
                .withBusOrBusbarSectionId2("bbs1")
                .withPositionOrder2(121)
                .withDirection2(TOP)
                .build()
                .apply(network, true, reportNode);
        testReportNode(reportNode, "/reportNode/create-line-NB-without-extensions-report.txt");
    }

    @Test
    void testNoConnectableCreatedIfOrderPositionIsOutOfRangeAndLogOrThrowIfIncorrectPositionOrder() {
        LineAdder lineAdder = nbNetwork.newLine()
            .setId("lineTest")
            .setR(1.0)
            .setX(1.0)
            .setG1(0.0)
            .setG2(0.0)
            .setB1(0.0)
            .setB2(0.0);

        // order position is already taken
        CreateBranchFeederBays modificationWithPositionAlreadyTaken = new CreateBranchFeederBaysBuilder()
            .withBranchAdder(lineAdder)
            .withBusOrBusbarSectionId1("bbs2")
            .withPositionOrder1(79)
            .withLogOrThrowIfIncorrectPositionOrder1(true)
            .withBusOrBusbarSectionId2("bbs1_2")
            .withPositionOrder2(10)
            .withLogOrThrowIfIncorrectPositionOrder2(true)
            .build();
        modificationWithPositionAlreadyTaken.apply(nbNetwork);
        assertNull(nbNetwork.getLine("lineTest"));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modificationWithPositionAlreadyTaken.apply(nbNetwork, true, ReportNode.NO_OP));
        assertEquals("PositionOrder 10 already taken.", e.getMessage());

        // order position is too high
        CreateBranchFeederBays modificationWithPositionTooHigh = new CreateBranchFeederBaysBuilder()
            .withBranchAdder(lineAdder)
            .withBusOrBusbarSectionId1("bbs1")
            .withPositionOrder1(81)
            .withLogOrThrowIfIncorrectPositionOrder1(true)
            .withBusOrBusbarSectionId2("bbs1_2")
            .withPositionOrder2(11)
            .withLogOrThrowIfIncorrectPositionOrder2(true)
            .build();
        modificationWithPositionTooHigh.apply(nbNetwork);
        assertNull(nbNetwork.getLine("lineTest"));
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modificationWithPositionTooHigh.apply(nbNetwork, true, ReportNode.NO_OP));
        assertEquals("PositionOrder 81 too high (>79).", e1.getMessage());

        // order position is too low
        CreateBranchFeederBays modificationWithPositionTooLow = new CreateBranchFeederBaysBuilder()
            .withBranchAdder(lineAdder)
            .withBusOrBusbarSectionId1("bbs2")
            .withPositionOrder1(12)
            .withLogOrThrowIfIncorrectPositionOrder1(true)
            .withBusOrBusbarSectionId2("bbs1_2")
            .withPositionOrder2(11)
            .withLogOrThrowIfIncorrectPositionOrder2(true)
            .build();
        modificationWithPositionTooLow.apply(nbNetwork);
        assertNull(nbNetwork.getLine("lineTest"));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modificationWithPositionTooLow.apply(nbNetwork, true, ReportNode.NO_OP));
        assertEquals("PositionOrder 12 too low (<71).", e2.getMessage());

        // no space on bbs1
        addIncoherentPositionBusBarSection(nbNetwork);

        CreateBranchFeederBays modificationWithNoSpace = new CreateBranchFeederBaysBuilder()
            .withBranchAdder(lineAdder)
            .withBusOrBusbarSectionId1("bbs1")
            .withPositionOrder1(71)
            .withLogOrThrowIfIncorrectPositionOrder1(true)
            .withBusOrBusbarSectionId2("bbs1_2")
            .withPositionOrder2(11)
            .withLogOrThrowIfIncorrectPositionOrder2(true)
            .build();
        modificationWithNoSpace.apply(nbNetwork);
        assertNull(nbNetwork.getLine("lineTest"));
        PowsyblException e3 = assertThrows(PowsyblException.class, () -> modificationWithNoSpace.apply(nbNetwork, true, ReportNode.NO_OP));
        assertEquals("Positions of adjacent busbar sections do not leave slots for new positions on busbar section 'bbs1'.", e3.getMessage());
    }

    private static void addIncoherentPositionBusBarSection(Network network) {
        VoltageLevel vl1 = network.getVoltageLevel("vl1");
        BusbarSection bbs = vl1.getNodeBreakerView().newBusbarSection().setId("extraBbs").setNode(40).add();
        Load load = vl1.newLoad().setId("extraLoad").setP0(0).setQ0(0).setNode(41).add();
        vl1.getNodeBreakerView().newSwitch().setId("extraDisconnector").setKind(SwitchKind.DISCONNECTOR).setNode1(40).setNode2(41).add();

        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPositionImpl(bbs, 1, 0));
        ConnectablePosition<Load> connectablePosition = new ConnectablePositionImpl<>(load,
            new ConnectablePositionImpl.FeederImpl("", 79), null, null, null);
        load.addExtension(ConnectablePosition.class, connectablePosition);
    }

    @Test
    void testGetName() {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        LineAdder lineAdder = network.newLine()
            .setId("lineTest")
            .setR(1.0)
            .setX(1.0)
            .setG1(0.0)
            .setG2(0.0)
            .setB1(0.0)
            .setB2(0.0);
        AbstractNetworkModification networkModification = new CreateBranchFeederBaysBuilder()
            .withBranchAdder(lineAdder)
            .withBusOrBusbarSectionId1("bbs5")
            .withPositionOrder1(115)
            .withDirection1(BOTTOM)
            .withBusOrBusbarSectionId2("bbs1")
            .withPositionOrder2(121)
            .withDirection2(TOP)
            .build();
        assertEquals("CreateBranchFeederBays", networkModification.getName());
    }
}
