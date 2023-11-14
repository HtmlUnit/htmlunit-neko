/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.htmlunit.cyberneko;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.htmlunit.cyberneko.util.HtmlEntities;
import org.htmlunit.cyberneko.util.HtmlEntities.Resolver;
import org.htmlunit.cyberneko.util.HtmlEntities2;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2  , timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class HtmlEntitiesParserBenchmark {

    final List<String> keys = new ArrayList<>();
    final List<String> values = new ArrayList<>();

    final Random r = new Random();

    @Setup
    public void setup(BenchmarkParams params) throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = HtmlEntitiesParserBenchmark.class.getResourceAsStream("html_entities.properties")) {
            props.load(stream);
        }

        props.forEach((k, v) -> {
            String key = (String) k;
            String value = (String) v;

            // we might have an empty line in it
            if (key.isEmpty()) {
                return;
            }

            keys.add(key + " ");
            values.add(value);
        });
    }

    @Benchmark
    public String oldParser() {
        String lastHit = null;

        for (int i = 0; i < keys.size(); i++) {
            HTMLEntitiesParser_Old parser = new HTMLEntitiesParser_Old();

            // already got a space at the end
            String parserInput = keys.get(i);
            int x = 0;
            while (parser.parse(parserInput.charAt(x))) {
                x++;
            }

            lastHit = parser.getMatch();
            if (!values.get(i).equals(lastHit))
            {
                throw new RuntimeException("Darn");
            }
        }

        return lastHit;
    }

    @Benchmark
    public String newParser1() {
        String lastHit = null;

        final Resolver resolver = new HtmlEntities.Resolver();

        for (int i = 0; i < keys.size(); i++) {
            // already got a space at the end
            String parserInput = keys.get(i);

            resolver.reset();

            int x = 0;
            while (resolver.parse(parserInput.charAt(x))) {
                x++;
            }

            lastHit = resolver.getResolvedValue();
            if (!values.get(i).equals(lastHit))
            {
                throw new RuntimeException("Darn");
            }
        }

        return lastHit;
    }

    @Benchmark
    public String newParser2() {
        String lastHit = null;
        HtmlEntities2.get();

        for (int i = 0; i < keys.size(); i++) {
            // already got a space at the end
            String parserInput = keys.get(i);

            HtmlEntities2.Level result = null;

            for (int x = 0; ; x++) {
                HtmlEntities2.Level r = HtmlEntities2.get().lookup(parserInput.charAt(x), result);
                if (r.endNode) {
                    result = r;
                    break;
                }
                else if (result == r) {
                    // no more stuff to match, last one was the last match
                    break;
                }
                result = r;
            }

            lastHit = result.resolvedValue;
            if (!values.get(i).equals(lastHit))
            {
                throw new RuntimeException("Darn");
            }
        }

        return lastHit;
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                // important, otherwise we will run all tests!
                .include(HtmlEntitiesParserBenchmark.class.getSimpleName() + ".newParser2")
                // 0 is needed for debugging, not for running
                .forks(0)
                .build();

        new Runner(opt).run();
    }
}

