package io.quarkiverse.spec.generator.deployment.codegen;

import java.nio.file.Path;

import io.smallrye.config.common.utils.StringUtil;

public final class SpecCodeGenUtils {

    private SpecCodeGenUtils() {
    }

    public static String getSanitizedFileName(final Path specFilePath) {
        return StringUtil
                .replaceNonAlphanumericByUnderscores(
                        SpecGeneratorOutputPaths.getRelativePath(specFilePath).toString());
    }
}
