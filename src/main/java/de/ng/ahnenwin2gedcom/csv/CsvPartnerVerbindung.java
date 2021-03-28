package de.ng.ahnenwin2gedcom.csv;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CsvPartnerVerbindung {
    private static final Logger LOG = LoggerFactory.getLogger(CsvPartnerVerbindung.class);

    private final CsvAhne ahne;
    private final Map<VerbindungsColumn, Object> columns = new HashMap<>();

    CsvPartnerVerbindung(CsvAhne ahne, CSVRecord record) {
        this.ahne = ahne;
        setColumns(record);
    }

    private void setColumns(CSVRecord record) {
        for (var column : VerbindungsColumn.values()) setColumn(column, record);
    }

    private void setColumn(VerbindungsColumn column, CSVRecord record) {
        String value;
        try {
            value = record.get(column.getCsvValue());
        } catch (IllegalArgumentException ignore) {
            value = null;
        } catch (IllegalStateException exception) {
            value = null;
            LOG.error("IllegalStateException wurde geworfen. Das hätte nicht passieren dürfen.", exception);
        }
        if (column.getDatatype() == String.class) {
            columns.put(column, value);
        }
        else if (column.getDatatype() == Verbindung.class) {
            columns.put(column, Verbindung.findByCsvValue(value));
        }
        else {
            LOG.error("AhnenColumn {} has unhandled datatype {} for a CsvAhne.",
                    column, column.getDatatype().getSimpleName());
        }
    }

    public String getString(VerbindungsColumn verbindungsColumn) {
        return get(verbindungsColumn, String.class);
    }

    public Verbindung getVerbindung() {
        return get(VerbindungsColumn.VERBINDUNG, Verbindung.class);
    }

    private <T> T get(VerbindungsColumn key, Class<T> requiredDatatype) {
        if (key.getDatatype().equals(requiredDatatype)) {
            return requiredDatatype.cast(columns.get(key));
        }
        throw new RuntimeException(String.format("Requires datatype %s, but was %s",
                requiredDatatype,
                key.getDatatype()));
    }

    public Integer getPartnerNummer() {
        String csvValue = getString(VerbindungsColumn.PARTNER);
        String trimmedCsvValue = csvValue == null ? "" : csvValue.trim();
        String partnerId = trimmedCsvValue.replaceAll("^.+\\((\\d+)\\)$","$1");
        try {
            return Integer.parseInt(partnerId);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    boolean hasPartnerId() {
        return getPartnerNummer() != null;
    }

    public CsvAhne getAhne() {
        return ahne;
    }
}
