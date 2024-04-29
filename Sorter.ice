module Demo {
    sequence<string> StringSeq;

    interface WorkerI {
        StringSeq doTask(StringSeq tasks);
        void mergeFiles(string file1, string file2, string outputFile);
    };
};