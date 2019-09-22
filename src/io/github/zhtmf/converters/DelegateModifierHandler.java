package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;

import io.github.zhtmf.converters.auxiliary.ModifierHandler;
import io.github.zhtmf.converters.auxiliary.exceptions.UnsatisfiedConstraintException;

class DelegateModifierHandler<T> extends ModifierHandler<T>{
    
    private ModifierHandler<T> impl;
    
    public DelegateModifierHandler(ModifierHandler<T> impl) {
        this.impl = impl;
    }
    
    boolean checkLength = false;
    
    public static interface ModifierHandlerAccess{
        public void setCurrentPosition(int pos);
    }
    
    public static ModifierHandlerAccess access;
    
    @Override
    public T handleDeserialize0(String fieldName, Object entity, InputStream is) {
        is.mark(0);
        T ret = null;
        try {
            impl.currentPosition.set(((MarkableInputStream)is).actuallyProcessedBytes());
            ret = impl.handleDeserialize0(fieldName, entity, is);
            impl.currentPosition.set(-1);
            checkReturnValue(ret);
            is.reset();
        } catch (IOException e) {
            throw new UnsatisfiedConstraintException(e)
                .withSiteAndOrdinal(ModifierHandler.class, 3);
        }
        return ret;
    }

    @Override
    public T handleSerialize0(String fieldName, Object entity) {
        T ret = impl.handleSerialize0(fieldName, entity);
        checkReturnValue(ret);
        return ret;
    }
    
    private void checkReturnValue(T ret) {
        if(ret==null) {
            throw new UnsatisfiedConstraintException("should return non-null value from handler "+this.getClass())
                    .withSiteAndOrdinal(ModifierHandler.class, 1);
        }else if(checkLength && ((Integer)ret)<0) {
            throw new UnsatisfiedConstraintException("should return positive value from handler "+this.getClass())
            .withSiteAndOrdinal(ModifierHandler.class, 2);
        }
    }

}
