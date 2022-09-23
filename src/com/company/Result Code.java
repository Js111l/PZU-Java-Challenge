package com.company;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {

    public static void main(String[] args) {
        String input = "SKIPwarszawa;oc;zdrowotne\n"
                + "bielsko-biała;na życie ;od powodzi\n"
                + "łódź;  od ognia;OD NIESZCZĘŚLIWYCH WYPADKÓW;ac\n\n"
                + "  ŁÓDŹ;domu;na wypadek straty pracy;Ac";

        Map<String, String[]> expectedOutput = Map.of("łódź",
                new String[] { "od ognia", "od nieszczęśliwych wypadków", "ac", "domu", "na wypadek straty pracy" }, "bielsko-biała",
                new String[] { "na życie", "od powodzi" });

        InputStream inputStream = new java.io.ByteArrayInputStream(input.getBytes());
        InsuranceServiceGrouping grouping = new InsuranceServiceGrouping();
        Map<String, String[]> output = grouping.processFile(inputStream);

        //proste testy
        boolean b=(output.size()==expectedOutput.size());
        boolean a =(output.entrySet().stream().allMatch(e -> {
            String[] expectedOutputArray = expectedOutput.get(e.getKey());
            if (expectedOutputArray == null) {
                return false;
            }
            String[] outputArray = e.getValue();
            java.util.Arrays.sort(outputArray);
            java.util.Arrays.sort(expectedOutputArray);
            return java.util.Arrays.equals(outputArray, expectedOutputArray);
        }));
        System.out.println(a+" "+b);
    }
}
//       1.każde miasto i usługa były pozbawione pustych znaków z przodu i z tyłu
//       2.prawidłowo grupował usługi nawet jeśli ich definicje powtarzają się w kilku linijkach i są pisane literami różnej wielkości (sprowadzał nazwy usług i miast do małych liter)
//       3.usuwał duplikaty usług w ramach jednego miasta, nawet jeśli są one pisane literami różnej wielkości
//       4.ignorował puste linie
//       5.ignorował linie które zaczynają się od słowa SKIP
//       6.działał zgodnie z przykładem

class Scratch {
    public static void main(String[] args) {

        String input = "SKIPwarszawa;oc;zdrowotne\n"
                + "bielsko-biała;na życie ;od powodzi\n"
                + "łódź;  od ognia;OD NIESZCZĘŚLIWYCH WYPADKÓW;ac\n\n"
                + "  ŁÓDŹ;domu;na wypadek straty pracy;Ac";

        Map<String, String[]> expectedOutput = Map.of("łódź",
                new String[] { "od ognia", "od nieszczęśliwych wypadków", "ac", "domu", "na wypadek straty pracy" }, "bielsko-biała",
                new String[] { "na życie", "od powodzi" });

        InputStream inputStream = new java.io.ByteArrayInputStream(input.getBytes());
        InsuranceServiceGrouping grouping = new InsuranceServiceGrouping();
        Map<String, String[]> output = grouping.processFile(inputStream);
        Set<String> set=output.keySet();
        for (String s:set
        ) {
            System.out.println(s);
        }

        System.out.println(output.size());

        boolean b=(output.size()==expectedOutput.size());
        boolean a =(output.entrySet().stream().allMatch(e -> {
            String[] expectedOutputArray = expectedOutput.get(e.getKey());
            if (expectedOutputArray == null) {
                return false;
            }
            String[] outputArray = e.getValue();
            java.util.Arrays.sort(outputArray);
            java.util.Arrays.sort(expectedOutputArray);
            return java.util.Arrays.equals(outputArray, expectedOutputArray);
        }));

    }
}


class InsuranceServiceGrouping {
    Map<String, String[]> processFile(InputStream inputStream)  {
        Predicate<String> filter = line -> !line.startsWith("SKIP".
                toLowerCase(Locale.ROOT))||!line.startsWith("SKIP".toUpperCase());

        Function<String, String[]> mapper = line -> Arrays.stream(line.
                        toLowerCase().split(";")).
                map(x->x.trim()).collect(Collectors.toList()).toArray(String[]::new);

        Collector<String[], ?, Map<String,
                String[]>> collector = Collectors.toMap(elem -> elem[0],
                elem -> Arrays.stream(elem).dropWhile(x -> x == elem[0]).toArray(String[]::new));

        StreamProcessor processor = new StreamProcessor.StreamProcessorBuilder().filter(filter).mapper(mapper)
                .collector(collector).build();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> strings = new ArrayList<>();

        return processFile(bufferedReader, processor);
    }

    private boolean helper(String line) {
        for (int i = 0; i <line.length() ; i++) {
            if(!Character.isLetter(line.charAt(i))){
                return false;
            }
        }
        return true;
    }

    Map<String, String[]> processFile(BufferedReader bufferedReader, StreamProcessor processor) {
        Map<String, String[]> outputMap=new HashMap<>();
        while (true) {
            try {
                if (!bufferedReader.ready()) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            String buffer= null;
            try {
                buffer = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Map<String,String[]> map= Stream.of(buffer).
                    filter(processor.getFilter()).map(processor.getMapper()).
                    collect(processor.getCollector());
            if(!map.isEmpty()) {
                String key = map.keySet().stream().collect(Collectors.toList()).get(0);
                if (!outputMap.isEmpty() && outputMap.keySet().contains(key)) {
                    Set<String> set = Arrays.stream(map.get(key)).collect(Collectors.toSet());
                    List<String> list = set.stream().collect(Collectors.toList());
                    Collections.addAll(list, outputMap.get(key));
                    outputMap.replace(key, outputMap.get(key), list.stream().collect(Collectors.toSet()).
                            stream().toArray(String[]::new));
                } else {
                    map.put(key,Arrays.
                            stream(map.get(key)).collect(Collectors.toList()).toArray(String[]::new));
                    outputMap.putAll(map);
                }
            }
        }
        if(outputMap.containsKey("")){
            outputMap.remove("");
        }
        return outputMap;
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

