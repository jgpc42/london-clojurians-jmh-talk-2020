### What is it?

This repository contains the example code, data, and other materials for the [JMH][jmh] and [jmh-clojure][jmh-clj] talk given at a London Clojurians online streaming event on [2020-07-28][event].

A capture is available on YouTube, and can be [found here][youtube].

The [`org-mode`][org] presentation slides are available in the `slides/` directory.

### Run the comparison benchmarks

`cd` to the root directory and run:

```bash
bash run.sh $TOTAL_RUNS
```

This script will do 10 full runs by default and requires CLI tools (`clj`) in your `$PATH`. It usually will take hours to complete (depending on your hardware) and will generate quite a bit of output (`edn` data), so you may want to pipe it to a file.

The results of running this script on my machine are in `results.edn`. Samples of which were used in the talk.

Alternatively, you can also run the benchmark suite via Leiningen:

```bash
lein bench-all
```

Or a specific test like:

```bash
lein bench :criterium :interleaved
```

### Run the example benchmarks

These require the [lein-jmh][lein-jmh] plugin as described in the talk.

```bash
lein compile
lein jmh '{:file "xx-example.edn", #_...}'
```

Where `xx-example.edn` is one of the example benchmark data files. Replace `#_...` with your desired options.

Running the [JFR][jfr] external profiler [example][ex-prof] requires a recent JDK. I was using OpenJDK 14 on Ubuntu.

### More information

Please see the [jmh-clojure][jmh-clj] project for more.

### License

Copyright © 2020 Justin Conklin

Distributed under the Eclipse Public License, the same as Clojure.



[event]:     https://www.meetup.com/London-Clojurians/events/271860420/
[ex-prof]:   https://github.com/jgpc42/london-clojurians-jmh-talk-2020/blob/master/src/demo/jfr.clj
[jfr]:       https://en.wikipedia.org/wiki/JDK_Flight_Recorder
[jmh]:       http://openjdk.java.net/projects/code-tools/jmh
[jmh-clj]:   https://github.com/jgpc42/jmh-clojure
[lein-jmh]:  https://github.com/jgpc42/lein-jmh
[org]:       https://orgmode.org/
[youtube]:   https://www.youtube.com/watch?v=_6qVfFkBdWI
