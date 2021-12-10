/**
 *
 */
module info.kgeorgiy.ja.Andreev.implementor {
    requires java.compiler;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.concurrent;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;
    exports info.kgeorgiy.ja.Andreev.implementor;
    exports info.kgeorgiy.ja.Andreev.arrayset;
    exports info.kgeorgiy.ja.Andreev.concurrent;
    exports info.kgeorgiy.ja.Andreev.student;
    exports info.kgeorgiy.ja.Andreev.walk;
    exports info.kgeorgiy.ja.Andreev.crawler;
    exports info.kgeorgiy.ja.Andreev.hello;
    exports info.kgeorgiy.ja.Andreev.textStatistics;
    opens info.kgeorgiy.ja.Andreev.implementor  to junit;
}