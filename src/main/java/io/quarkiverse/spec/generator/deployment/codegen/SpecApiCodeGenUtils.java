package io.quarkiverse.spec.generator.deployment.codegen;

import java.nio.file.Path;

import io.smallrye.config.common.utils.StringUtil;

public final class SpecApiCodeGenUtils {

    private SpecApiCodeGenUtils() {
    }

    public static String getSanitizedFileName(final Path specFilePath) {
        return StringUtil
                .replaceNonAlphanumericByUnderscores(
                        SpecApiGeneratorOutputPaths.getRelativePath(specFilePath).toString());
    }
}
