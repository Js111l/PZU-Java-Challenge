package com.company;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Default_Code {
    public static void main(String[] args) throws IOException {
        String input = "SKIPwarszawa;oc;zdrowotne\n"
                + "bielsko-biała;na życie ;od powodzi\n"
                + "łódź;  od ognia;OD NIESZCZĘŚLIWYCH WYPADKÓW;ac\n\n"
                + "  ŁÓDŹ;domu;na wypadek straty pracy;Ac";

        InputStream inputStream = new java.io.ByteArrayInputStream(input.getBytes());
        InsuranceServiceGrouping grouping = new InsuranceServiceGrouping();
        Map<String, String[]> output = grouping.processFile(inputStream);
    }

}

class InsuranceServiceGrouping {

    Map<String, String[]> processFile(InputStream inputStream) throws IOException {

        Predicate<String> filter = x -> x.substring(1, 4) != "SKIP";

        Function<String, String[]> mapper = line -> Arrays.stream(line.split(";")).toArray(String[]::new);

        Collector<String[], ?, Map<String, String[]>> collector = Collectors.toMap(elem -> elem[0],
                elem -> new String[] { elem[1], elem[2] });

        StreamProcessor processor = new StreamProcessor.StreamProcessorBuilder().filter(filter).mapper(mapper)
                .collector(collector).build();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        return processFile(bufferedReader, processor);
    }

    Map<String, String[]> processFile(BufferedReader bufferedReader, StreamProcessor processor) throws IOException {
        int c=0;
        StringBuilder stringBuilder=new StringBuilder();
        while ((c=bufferedReader.read())!=-1){
            stringBuilder.append((char)c);
        }
        String word=stringBuilder.toString();


        return word.lines().filter(processor.getFilter()).map(processor.getMapper())
                .collect(processor.getCollector());
    }
}

class StreamProcessor {
    private final Predicate<String> filter;
    private final Function<String, String[]> mapper;
    private final Collector<String[], ?, Map<String, String[]>> collector;

    StreamProcessor() {
        this.filter = null;
        this.mapper = null;
        this.collector = null;
    }

    StreamProcessor(StreamProcessorBuilder builder) {
        this.filter = builder.filter;
        this.mapper = builder.mapper;
        this.collector = builder.collector;
    }

    public static class StreamProcessorBuilder {
        private Predicate<String> filter;
        private Function<String, String[]> mapper;
        private Collector<String[], ?, Map<String, String[]>> collector;

        StreamProcessorBuilder filter(Predicate<String> filter) {
            this.filter = filter;
            return this;
        }

        StreamProcessorBuilder mapper(Function<String, String[]> mapper) {
            this.mapper = mapper;
            return this;
        }

        StreamProcessorBuilder collector(Collector<String[], ?, Map<String, String[]>> collector) {
            this.collector = collector;
            return this;
        }

        StreamProcessor build() {
            return new StreamProcessor(this);
        }
    }

    Predicate<String> getFilter() {
        return filter;
    }

    Function<String, String[]> getMapper() {
        return mapper;
    }

    Collector<String[], ?, Map<String, String[]>> getCollector() {
        return collector;
    }
}
