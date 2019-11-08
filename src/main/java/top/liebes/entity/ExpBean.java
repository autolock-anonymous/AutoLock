package top.liebes.entity;

import java.util.HashMap;
import java.util.Map;

public class ExpBean {
    private long sip4jAnalysisTime;

    private long inferLockTime;

    private long applyLockTime;

    private int numberOfPure;

    private int numberOfShare;

    private int numberOfFull;

    private int numberOfUnique;

    private int numberOfImmutable;

    private int numberOfClass;

    private int numberOfMethod;

    private int newLockDeclaration;

    private int totalLockDeclaration;

    private int newLockInsertion;

    private int totalLockInsertion;

    private Map<String, Integer> fieldsCount;

    public ExpBean() {
        this.sip4jAnalysisTime = 0;
        this.inferLockTime = 0;
        this.applyLockTime = 0;
        this.numberOfPure = 0;
        this.numberOfShare = 0;
        this.numberOfFull = 0;
        this.numberOfImmutable = 0;
        this.numberOfClass = -1;
        this.numberOfMethod = -1;
        this.newLockDeclaration = 0;
        this.totalLockDeclaration = 0;
        this.newLockInsertion = 0;
        this.totalLockInsertion = 0;
        this.fieldsCount = new HashMap<>();
    }

    public long getSip4jAnalysisTime() {
        return sip4jAnalysisTime;
    }

    public void setSip4jAnalysisTime(long sip4jAnalysisTime) {
        this.sip4jAnalysisTime = sip4jAnalysisTime;
    }

    public long getInferLockTime() {
        return inferLockTime;
    }

    public void setInferLockTime(long inferLockTime) {
        this.inferLockTime = inferLockTime;
    }

    public long getApplyLockTime() {
        return applyLockTime;
    }

    public void setApplyLockTime(long applyLockTime) {
        this.applyLockTime = applyLockTime;
    }

    public int getNumberOfPure() {
        return numberOfPure;
    }

    public void setNumberOfPure(int numberOfPure) {
        this.numberOfPure = numberOfPure;
    }

    public int getNumberOfShare() {
        return numberOfShare;
    }

    public void setNumberOfShare(int numberOfShare) {
        this.numberOfShare = numberOfShare;
    }

    public int getNumberOfFull() {
        return numberOfFull;
    }

    public void setNumberOfFull(int numberOfFull) {
        this.numberOfFull = numberOfFull;
    }

    public int getNumberOfImmutable() {
        return numberOfImmutable;
    }

    public void setNumberOfImmutable(int numberOfImmutable) {
        this.numberOfImmutable = numberOfImmutable;
    }

    public int getNumberOfClass() {
        return numberOfClass;
    }

    public void setNumberOfClass(int numberOfClass) {
        this.numberOfClass = numberOfClass;
    }

    public int getNumberOfMethod() {
        return numberOfMethod;
    }

    public void setNumberOfMethod(int numberOfMethod) {
        this.numberOfMethod = numberOfMethod;
    }

    public int getNewLockDeclaration() {
        return newLockDeclaration;
    }

    public void setNewLockDeclaration(int newLockDeclaration) {
        this.newLockDeclaration = newLockDeclaration;
    }

    public int getTotalLockDeclaration() {
        return totalLockDeclaration;
    }

    public void setTotalLockDeclaration(int totalLockDeclaration) {
        this.totalLockDeclaration = totalLockDeclaration;
    }

    public int getNewLockInsertion() {
        return newLockInsertion;
    }

    public void setNewLockInsertion(int newLockInsertion) {
        this.newLockInsertion = newLockInsertion;
    }

    public int getTotalLockInsertion() {
        return totalLockInsertion;
    }

    public void setTotalLockInsertion(int totalLockInsertion) {
        this.totalLockInsertion = totalLockInsertion;
    }

    public int getNumberOfUnique() {
        return numberOfUnique;
    }

    public void setNumberOfUnique(int numberOfUnique) {
        this.numberOfUnique = numberOfUnique;
    }

    public Map<String, Integer> getFieldsCount() {
        return fieldsCount;
    }

    public void setFieldsCount(Map<String, Integer> fieldsCount) {
        this.fieldsCount = fieldsCount;
    }
}
