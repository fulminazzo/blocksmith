package it.fulminazzo.blocksmith.data.mapper;

final class ExceptionMapper extends AbstractMapper {

    public ExceptionMapper() throws Exception {
        throw new Exception("Test exception");
    }

}
