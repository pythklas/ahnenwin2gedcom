package de.ng.ahnenwin2gedcom.csv;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CsvAhne {
    private static final Logger LOG = LoggerFactory.getLogger(CsvAhne.class);

    private final Map<AhnenColumn, Object> ahnenColumns = new HashMap<>();
    private final Set<CsvPartnerVerbindung> partnerVerbindungen = new HashSet<>();

    CsvAhne(List<CSVRecord> records) {
        CSVRecord mainRecord = records.get(0);
        setColumnValues(mainRecord);
        setPartnerVerbindungen(records);
    }

    private void setPartnerVerbindungen(Collection<CSVRecord> records) {
        records.stream()
                .map(record -> new CsvPartnerVerbindung(this, record))
                .filter(CsvPartnerVerbindung::hasPartnerId)
                .forEach(partnerVerbindungen::add);
    }

    private void setColumnValues(CSVRecord record) {
        for (var column : AhnenColumn.values()) setColumnValue(column, record);
    }

    private void setColumnValue(AhnenColumn column, CSVRecord record) {
        String value;

        try {
            value = record.get(column.getCsvValue());
        }
        catch (IllegalArgumentException ignore) {
            value = null;
        }

        if (column.getDatatype() == int.class) {
            if (value == null) {
                throw new IllegalStateException("Column datatype was int, but provided value was null.");
            }
            ahnenColumns.put(column, Integer.parseInt(value));
            return;
        }

        if (column.getDatatype() == String.class) {
            ahnenColumns.put(column, value);
            return;
        }

        if (column.getDatatype() == Geschlecht.class) {
            ahnenColumns.put(column, Geschlecht.findByCsvValue(value));
            return;
        }

        LOG.error("AhnenColumn {} has unhandled datatype {} for a CsvAhne.",
                column, column.getDatatype().getSimpleName());

    }

    public String getString(AhnenColumn key) {
        return get(key, String.class);
    }

    public int getInt(AhnenColumn key) {
        if (key.getDatatype().equals(int.class)) {
            return (int) ahnenColumns.get(key);
        }
        throw new RuntimeException(String.format("Requires datatype %s, but was %s",
                int.class,
                key.getDatatype()));
    }

    public Geschlecht getGeschlecht() {
        return get(AhnenColumn.GESCHLECHT, Geschlecht.class);
    }

    public Set<CsvPartnerVerbindung> getPartnerVerbindungen() {
        return partnerVerbindungen;
    }

    private <T> T get(AhnenColumn key, Class<T> requiredDatatype) {
        if (key.getDatatype().equals(requiredDatatype)) {
            return requiredDatatype.cast(ahnenColumns.get(key));
        }
        throw new RuntimeException(String.format("Requires datatype %s, but was %s",
                requiredDatatype,
                key.getDatatype()));
    }

    @Override
    public String toString() {
        return String.format("CsvAhne{NUMMER=%s}", ahnenColumns.get(AhnenColumn.NUMMER));
    }
}
