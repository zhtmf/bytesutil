package io.github.zhtmf.script.test.test1;

public class TestObjectParent {

    protected int parentProtectedMethod() {
        return 31;
    }
    public int parentPublicMethod() {
        return 32;
    }
    public int getParentValue() {
        return 33;
    }
    @SuppressWarnings("unused")
    private int parentPrivateMethod() {
        return 34;
    }
}
