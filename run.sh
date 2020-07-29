#!/usr/bin/env bash
set -e

mkdir -p classes

opts=(
    ':jmh'
    ':criterium'
    ':jmh :interleaved'
    ':criterium :interleaved'
    ':criterium :interleaved :reversed'
)

runs=${1:-10}

for _ in $(seq "$runs"); do
    for args in "${opts[@]}"; do
        clj -J-Dclojure.compiler.direct-linking=true \
            -m bench $args
    done
done
