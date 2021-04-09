package de.ng.ahnenwin2gedcom.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvReader {
    private static final Predicate<CSVRecord> nummerColumnExists =
            csvRecord -> {
                try {
                    csvRecord.get(AhnenColumn.NUMMER.getCsvValue());
                    return true;
                }
                catch (IllegalArgumentException ignore) {
                    return false;
                }
            };


    /**
     * Reads the given ahnenwin 5 excel export file and maps it to a list of CsvAhne.
     * Note that the file actually is a tab separated csv file.
     * @param path The path of the ahnenwin 5 excel export file.
     * @return A list of CsvAhnen where each CsvAhne corresponds to an entry in the file.
     * Note that an entry can span several rows.
     * @throws IOException when there is a problem reading the file.
     */
    public static Set<CsvAhne> read(String path) throws IOException {
        Charset ansel = Charset.forName("windows-1252");
        Reader in = new FileReader(path, ansel);

        Iterable<CSVRecord> recordsIterable = CSVFormat.TDF
                .withHeader(header())
                .withQuote(null)
                .parse(in);

        CSVRecord[] records = StreamSupport.stream(recordsIterable.spliterator(), false)
                .filter(nummerColumnExists)
                .toArray(CSVRecord[]::new);

        return groupByNummer(records)
                .values()
                .stream()
                .map(CsvAhne::new)
                .collect(Collectors.toSet());
    }

    private static Map<Integer, List<CSVRecord>> groupByNummer(CSVRecord[] records) {
        Map<Integer, List<CSVRecord>> groupedRecords = new HashMap<>();

        for (int recordIndex = 0; recordIndex < records.length; ++recordIndex) {
            Integer nummer = getAhnenNummer(records, recordIndex);
            if (nummer == null) continue;
            groupedRecords
                    .computeIfAbsent(nummer, ignore -> new LinkedList<>())
                    .add(records[recordIndex]);
        }

        return groupedRecords;
    }

    private static Integer getAhnenNummer(CSVRecord[] records, int recordIndex) {
        Integer nummer = parseNummer(records[recordIndex]);
        return recordIndex == 0
                ? null
                : nummer == null
                    ? getAhnenNummer(records, recordIndex - 1)
                    : nummer;
    }

    private static Integer parseNummer(CSVRecord record) {
        try {
            return Integer.parseInt(record.get(AhnenColumn.NUMMER.getCsvValue()));
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static String[] header() {
        return Stream.<CsvColumn[]>builder()
                .add(AhnenColumn.values())
                .add(VerbindungsColumn.values())
                .build()
                .flatMap(Arrays::stream)
                .map(CsvColumn::getCsvValue)
                .toArray(String[]::new);
    }
}
