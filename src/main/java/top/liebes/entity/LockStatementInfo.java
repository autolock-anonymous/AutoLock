package top.liebes.entity;

public class LockStatementInfo {

    public final static int READ_LOCK = 1;
    public final static int WRITE_LOCK = 2;
    public final static int EXCLUSIVE_LOCK = 3;

    private int type;

    private String name;

    /**
     * true for lock, false for unlock
     */
    private boolean isLock;

    public LockStatementInfo(int type, String name, boolean isLock) {
        this.type = type;
        this.name = name;
        this.isLock = isLock;
    }

    public LockStatementInfo() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    public boolean isReadLock(){
        return this.type == READ_LOCK;
    }

    public boolean isWriteLock(){
        return this.type == WRITE_LOCK;
    }
}
