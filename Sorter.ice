module Demo {
    sequence<string> StringSeq;

    interface WorkerI {
        StringSeq doTask(StringSeq tasks);
    };
};