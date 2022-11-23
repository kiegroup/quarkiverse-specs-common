package io.quarkiverse.spec.generator.deployment.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
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
    private final SpecApiParameters constants;

    protected SpecApiGeneratorCodeGenBase(SpecCodeGenerator generator, SpecApiParameters constants) {
        this.generator = generator;
        this.constants = constants;
    }

    @Override
    public String inputDirectory() {
        return constants.getInputDirectory();
    }
    
    protected  Collection<String> excludedFiles(final Config config) {
    	return Collections.emptyList();
    }
    
    protected Collection<String> includedFiles(final Config config) {
    	return Collections.emptyList();
    }

    @Override
    public boolean trigger(CodeGenContext context) throws CodeGenException {
        final Path outDir = context.outDir();
        final Path specDir = context.inputDir();
        final Collection<String> ignoredFiles = excludedFiles(context.config());
        if (Files.isDirectory(specDir)) {
            try (Stream<Path> specFilePaths = Files.walk(specDir)) {
                specFilePaths
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.getFileName().toString();
                            return fileName.endsWith(inputExtension()) && !ignoredFiles.contains(fileName);
                        })
                        .forEach(specFilePath -> generator.generate(context.config(), specFilePath, outDir));
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
}
