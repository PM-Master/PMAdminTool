package gov.nist.csd.pm.admintool.tests;

import java.util.ArrayList;

public class SingletonActiveTests extends ArrayList<Test> {
    public static SingletonActiveTests tests;
    private SingletonActiveTests(){
        super();
        //Prevent form the reflection api.
        if (tests != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public synchronized static SingletonActiveTests getInstance() {
        if (tests == null){ // if there is no instance available... create new one
            tests = new SingletonActiveTests();
        }
        return tests;
    }

    public static boolean addTest(Test testToAdd) {
        return tests.add(testToAdd);
    }

    public static boolean removeTest(Test testToRemove) {
        return tests.remove(testToRemove);
    }

    public static void removeAllTests() {
        for (int i = 0; i < tests.size(); i++) {
            tests.remove(0);
        }
    }
}
