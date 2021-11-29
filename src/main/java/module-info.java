module edu.kit.ipasir4j {

    requires java.compiler;
    requires com.google.auto.service;
    requires transitive jdk.incubator.foreign;

    exports edu.kit.ipasir4j;
    exports edu.kit.ipasir4j.callback;

}