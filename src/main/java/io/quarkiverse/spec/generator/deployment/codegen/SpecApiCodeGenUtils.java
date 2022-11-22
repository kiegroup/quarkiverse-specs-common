package io.quarkiverse.spec.generator.deployment.codegen;

import java.nio.file.Path;

import io.smallrye.config.common.utils.StringUtil;

public final class SpecApiCodeGenUtils {

    private SpecApiCodeGenUtils() {
    }

    private static final String BUILD_TIME_SPEC_PREFIX_FORMAT = "quarkus.%s.spec.%s";
    private static final String BASE_PACKAGE_PROP_FORMAT = "%s.base-package";
    private static final String IGNORED_FILES_PROP_FORMAT = "quarkus.%s.codegen.ignore";

    public static String getSanitizedFileName(final Path specFilePath) {
        return StringUtil
                .replaceNonAlphanumericByUnderscores(
                        SpecApiGeneratorOutputPaths.getRelativePath(specFilePath).toString());
    }

    public static String getBuildTimeSpecPropertyPrefix(final Path specFilePath, String configPrefix) {
        return String.format(BUILD_TIME_SPEC_PREFIX_FORMAT, configPrefix, getSanitizedFileName(specFilePath));
    }
    
    public static String getIgnoredFilesPropFormat (String configPrefix) {
    	return String.format(IGNORED_FILES_PROP_FORMAT, configPrefix);
    }

    public static String getBasePackagePropertyName(final Path specFilePath, String configPrefix) {
        return String.format(BASE_PACKAGE_PROP_FORMAT, getBuildTimeSpecPropertyPrefix(specFilePath, configPrefix));
    }

}
