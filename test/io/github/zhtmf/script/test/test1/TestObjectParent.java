package io.github.zhtmf.script.test.test1;

public class TestObjectParent {
	public int parentFieldPublic = 1;
	protected int parentFieldProtected = 2;
	int parentFieldPackage = 3;
	@SuppressWarnings("unused")
	private int parentFieldPrivate = 4;
	
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
