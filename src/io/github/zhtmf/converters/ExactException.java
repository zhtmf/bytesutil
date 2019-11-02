package io.github.zhtmf.converters;

/**
 * Common interface of exceptions for test purposes
 * 
 * @author dzh
 */
interface ExactException {
    Class<?> getSite();
    int getOrdinal();
}
