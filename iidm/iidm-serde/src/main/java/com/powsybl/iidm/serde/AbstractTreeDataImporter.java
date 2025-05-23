/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.base.Joiner;
import com.google.common.base.Suppliers;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.extensions.ExtensionProvider;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractTreeDataImporter implements Importer {

    private static final Supplier<ExtensionProviders<ExtensionSerDe>> EXTENSIONS_SUPPLIER = Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerDe.class, "network"));

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTreeDataImporter.class);

    public static final String THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND = "iidm.import.xml.throw-exception-if-extension-not-found";

    public static final String EXTENSIONS_LIST = "iidm.import.xml.extensions";

    public static final String WITH_AUTOMATION_SYSTEMS = "iidm.import.xml.with-automation-systems";

    public static final String MISSING_PERMANENT_LIMIT_PERCENTAGE = "iidm.import.xml.missing-permanent-limit-percentage";

    public static final String MINIMAL_VALIDATION_LEVEL = "iidm.import.minimal-validation-level";

    private static final Parameter THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER
            = new Parameter(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, ParameterType.BOOLEAN, "Throw exception if extension not found", Boolean.FALSE)
            .addAdditionalNames("throwExceptionIfExtensionNotFound");

    private static final Parameter EXTENSIONS_LIST_PARAMETER
            = new Parameter(EXTENSIONS_LIST, ParameterType.STRING_LIST, "The list of extension files ", null,
            EXTENSIONS_SUPPLIER.get().getProviders().stream().map(ExtensionProvider::getExtensionName).collect(Collectors.toList()));

    private static final Parameter WITH_AUTOMATION_SYSTEMS_PARAMETER = new Parameter(WITH_AUTOMATION_SYSTEMS, ParameterType.BOOLEAN,
            "Import network with automation systems", Boolean.TRUE);

    public static final Parameter MISSING_PERMANENT_LIMIT_PERCENTAGE_PARAMETER = new Parameter(MISSING_PERMANENT_LIMIT_PERCENTAGE,
            ParameterType.DOUBLE, "Percentage applied to lowest temporary limit to compute the permanent limit when missing (for IIDM < 1.12 only)",
            100.);

    public static final Parameter MINIMAL_VALIDATION_LEVEL_PARAMETER = new Parameter(MINIMAL_VALIDATION_LEVEL,
            ParameterType.STRING, "Minimal validation level accepted",
            null);

    private final ParameterDefaultValueConfig defaultValueConfig;

    static final String SUFFIX_MAPPING = "_mapping";

    protected AbstractTreeDataImporter() {
        this(PlatformConfig.defaultConfig());
    }

    protected AbstractTreeDataImporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = List.of(THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, EXTENSIONS_LIST_PARAMETER,
                WITH_AUTOMATION_SYSTEMS_PARAMETER, MISSING_PERMANENT_LIMIT_PERCENTAGE_PARAMETER,
                MINIMAL_VALIDATION_LEVEL_PARAMETER);
        return ConfiguredParameter.load(parameters, getFormat(), defaultValueConfig);
    }

    private String findExtension(ReadOnlyDataSource dataSource) throws IOException {
        for (String ext : getExtensions()) {
            if (dataSource.isDataExtension(ext) && dataSource.exists(null, ext)) {
                return ext;
            }
        }
        return null;
    }

    protected abstract String[] getExtensions();

    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(getExtensions());
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource);
            return exists(dataSource, ext);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected abstract boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException;

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        try {
            String ext = findExtension(fromDataSource);
            if (!exists(fromDataSource, ext)) {
                throw new PowsyblException("From data source is not importable");
            }
            // copy iidm file
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null, ext, false)) {
                ByteStreams.copy(is, os);
            }
            // and also anonymization file if exists
            if (fromDataSource.exists(SUFFIX_MAPPING, "csv")) {
                try (InputStream is = fromDataSource.newInputStream(SUFFIX_MAPPING, "csv");
                     OutputStream os = toDataSource.newOutputStream(SUFFIX_MAPPING, "csv", false)) {
                    ByteStreams.copy(is, os);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reportNode);
        Network network;

        ImportOptions options = createImportOptions(parameters);
        long startTime = System.currentTimeMillis();
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new PowsyblException("File " + dataSource.getBaseName()
                        + "." + Joiner.on("|").join(getExtensions()) + " not found");
            }

            network = NetworkSerDe.read(dataSource, networkFactory, options, ext, reportNode);
            ReportNode subReportNode = DeserializerReports.doneImportingXiidm(reportNode);
            DeserializerReports.importedNetworkReport(subReportNode, network.getId(), options.getFormat().toString());
            LOGGER.debug("{} import done in {} ms", getFormat(), System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            throw new PowsyblException(e);
        }
        return network;
    }

    protected ImportOptions createImportOptions(Properties parameters) {
        return new ImportOptions()
                .setThrowExceptionIfExtensionNotFound(Parameter.readBoolean(getFormat(), parameters, THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND_PARAMETER, defaultValueConfig))
                .setExtensions(Parameter.readStringList(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig) != null ? new HashSet<>(Parameter.readStringList(getFormat(), parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig)) : null)
                .setWithAutomationSystems(Parameter.readBoolean(getFormat(), parameters, WITH_AUTOMATION_SYSTEMS_PARAMETER, defaultValueConfig))
                .setMissingPermanentLimitPercentage(Parameter.readDouble(getFormat(), parameters, MISSING_PERMANENT_LIMIT_PERCENTAGE_PARAMETER, defaultValueConfig))
                .setMinimalValidationLevel(Parameter.readString(getFormat(), parameters, MINIMAL_VALIDATION_LEVEL_PARAMETER, defaultValueConfig));
    }
}

