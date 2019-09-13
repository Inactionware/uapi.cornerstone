/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal;

import com.fasterxml.jackson.jr.ob.JSON;
import uapi.service.Tags;
import uapi.common.Functionals;
import uapi.config.IConfigFileParser;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;
import uapi.log.ILogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * The parser used to parse JSON format file
 */
@Service({ IConfigFileParser.class })
@Tag(Tags.CONFIG)
public class JsonFileParser implements IConfigFileParser {

    private static final String JSON_FILE_EXT   = "json";

    @Inject
    ILogger _logger;

    @Override
    public boolean isSupport(String fileExtension) {
        return JSON_FILE_EXT.equalsIgnoreCase(fileExtension);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> parse(File configFile) {
        try {
            return extract(configFile, input -> {
                JSON.std.with(JSON.Feature.READ_ONLY);
                return JSON.std.mapFrom(input);
            });
        } catch (IOException ex) {
            this._logger.error(ex, "Parse file {} failed", configFile.getName());
        }
        return null;
    }

    private Map extract(
            final File file,
            final Functionals.Extractor<FileInputStream, Map, IOException> extractor
    ) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            return extractor.accept(input);
        }
    }
}
