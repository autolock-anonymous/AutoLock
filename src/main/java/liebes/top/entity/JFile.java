package liebes.top.entity;

import sip4j.graphstructure.E_ClassGraphs;

import java.io.File;
import java.util.*;

/**
 * @author liebes
 */
public class JFile {

    private File file;

    private List<E_ClassGraphs> classGraphs;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<E_ClassGraphs> getClassGraphs() {
        return classGraphs;
    }

    public void setClassGraphs(List<E_ClassGraphs> classGraphs) {
        this.classGraphs = classGraphs;
    }



}
