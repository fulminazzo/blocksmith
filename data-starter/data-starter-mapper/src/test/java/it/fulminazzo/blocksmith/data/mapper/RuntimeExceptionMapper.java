package it.fulminazzo.blocksmith.data.mapper;

final class RuntimeExceptionMapper extends AbstractMapper {

    public RuntimeExceptionMapper() {
        throw new RuntimeException("Test runtime exception");
    }

}
