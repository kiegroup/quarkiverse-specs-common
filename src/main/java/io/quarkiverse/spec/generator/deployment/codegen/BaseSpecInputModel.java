package io.quarkiverse.spec.generator.deployment.codegen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.PropertiesConfigSource;

import static java.util.Objects.requireNonNull;

public abstract class BaseSpecInputModel {

    private final InputStream inputStream;
    private final String fileName;
    private final Map<String, String> codegenProperties = new HashMap<>();

    protected abstract String getBasePackageNameProperty(String fileName);

    protected BaseSpecInputModel(final String fileName, final InputStream inputStream) {
        requireNonNull(inputStream, "InputStream can't be null");
        requireNonNull(fileName, "File name can't be null");
        this.inputStream = inputStream;
        this.fileName = fileName;
    }

    /**
     * @param filename the name of the file for reference
     * @param inputStream the content of the spec file
     * @param basePackageName the name of the package where the files will be generated
     */
    protected BaseSpecInputModel(final String fileName, final InputStream inputStream, final String basePackageName) {
        this(fileName, inputStream);
        this.codegenProperties.put(getBasePackageNameProperty(fileName), basePackageName);
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public ConfigSource getConfigSource() {
        return new PropertiesConfigSource(this.codegenProperties, "properties", 0);
    }

    @Override
    public String toString() {
        return "SpecInputModel{" +
                "name='" + fileName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseSpecInputModel that = (BaseSpecInputModel) o;
        return fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
