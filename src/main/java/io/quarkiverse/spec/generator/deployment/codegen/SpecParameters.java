package io.quarkiverse.spec.generator.deployment.codegen;

public abstract class SpecParameters {

    public static final String YAML = "yaml";
    public static final String YML = "yml";
    public static final String JSON = "json";
    public static final String STREAM = "stream";

    private final String providerPrefix;
    private final String inputDirectory;

    private final String extension;

    protected SpecParameters(String providerPrefix, String inputDirectory,
            String extension) {
        this.providerPrefix = providerPrefix;
        this.inputDirectory = inputDirectory;
        this.extension = extension;
    }

    public String getProviderPrefix() {
        return providerPrefix;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return "SpecApiParameters [providerPrefix=" + providerPrefix + ", inputDirectory=" + inputDirectory
                + ", extension=" + extension + "]";
    }

}
