#!/usr/bin/env bash
set -e

lein with-profile +jitwatch do uberjar

opts=(
    '-XX:+UnlockDiagnosticVMOptions'
    '-XX:+TraceClassLoading'
    '-XX:+LogCompilation'
    '-XX:+PrintAssembly'
    '-XX:PrintAssemblyOptions=intel-mnemonic'
)

java "${opts[@]}" -jar target/jitwatch.jar :criterium :interleaved
