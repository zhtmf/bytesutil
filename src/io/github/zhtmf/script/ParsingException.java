package io.github.zhtmf.script;

class ParsingException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private Class<?> site;
    private int ordinal;
    public ParsingException() {
    }
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    public ParsingException(String message) {
        super(message);
    }
    public ParsingException(Throwable cause) {
        super(cause);
        if(cause instanceof ParsingException) {
            this.site = ((ParsingException) cause).site;
            this.ordinal = ((ParsingException) cause).ordinal;
        }
    }
    public int getOrdinal() {
        return ordinal;
    }
    public Class<?> getSite() {
        return site;
    }
    public ParsingException withSiteAndOrdinal(Class<?> site, int ordinal) {
        if(this.site != null) {
            return this;
        }
        this.site = site;
        this.ordinal = ordinal;
        return this;
    }
    
    @Override
    public String getMessage() {
        if(super.getCause() instanceof ParsingException) {
            return super.getCause().getMessage();
        }
        return super.getMessage();
    }
    
    /**
     * Dedicated exception for terminating evaluation of script prematurely.
     * 
     * @author dzh
     */
    static class ParsingTerminationException extends ParsingException{
        private static final long serialVersionUID = 2L;
    }
}
