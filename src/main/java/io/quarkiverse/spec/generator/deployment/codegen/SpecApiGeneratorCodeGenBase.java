package io.quarkiverse.spec.generator.deployment.codegen;

import static io.quarkiverse.spec.generator.deployment.codegen.SpecApiCodeGenUtils.getBasePackagePropertyName;
import static io.quarkiverse.spec.generator.deployment.codegen.SpecApiCodeGenUtils.getSanitizedFileName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.Config;

import io.quarkus.bootstrap.prebuild.CodeGenException;
import io.quarkus.deployment.CodeGenContext;
import io.quarkus.deployment.CodeGenProvider;

/**
 * Base class for Spec generator.
 * 
 * It contain the code for scanning input files based on SpecApiContants provided by concrete extensions
 * It is responsible for invoking SpecCodeGenerator.generateCode. 
 *
 */
public abstract class SpecApiGeneratorCodeGenBase implements CodeGenProvider {

    protected final SpecCodeGenerator generator;
    private final SpecApiConstants constants;

    protected SpecApiGeneratorCodeGenBase(SpecCodeGenerator generator, SpecApiConstants constants) {
        this.generator = generator;
        this.constants = constants;
    }

    @Override
    public String inputDirectory() {
        return constants.getInputDirectory();
    }

    @Override
    public boolean trigger(CodeGenContext context) throws CodeGenException {
        final Path outDir = context.outDir();
        final Path specDir = context.inputDir();
        final List<String> ignoredFiles = context.config()
                .getOptionalValues(SpecApiCodeGenUtils.getIgnoredFilesPropFormat(constants.getConfigPrefix()) ,String.class).orElse(List.of());

        if (Files.isDirectory(specDir)) {
            try (Stream<Path> specFilePaths = Files.walk(specDir)) {
                specFilePaths
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.getFileName().toString();
                            return fileName.endsWith(inputExtension()) && !ignoredFiles.contains(fileName);
                        })
                        .forEach(specFilePath -> generator.generate(context.config(), specFilePath, outDir,
                                getBasePackage(context.config(), specFilePath)));
            } catch (IOException e) {
                throw new CodeGenException("Failed to generate java files from directory: " + specDir.toAbsolutePath(),
                        e);
            }
            return true;
        }
        return false;
    }

    @Override
    public String providerId() {
        return constants.getProviderPrefix() + "-" + constants.getExtension();
    }

    @Override
    public String inputExtension() {
        return "." + constants.getExtension();
    }

    protected final String getBasePackage(final Config config, final Path specFilePath) {
        return config
                .getOptionalValue(getBasePackagePropertyName(specFilePath, constants.getConfigPrefix()), String.class)
                .orElse(String.format("%s.%s", constants.getDefaultPackage(), getSanitizedFileName(specFilePath)));
    }
}
