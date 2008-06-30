//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.ecas.util;

//JDK imports
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import jpl.eda.util.DateConvert;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A set of utility methods and formatters for EDRN style dates. This class
 * contains methods from the W3C's Jigsaw software toolkit DateParser class.
 * 
 * http://www.w3.org/TR/1998/NOTE-datetime-19980827
 * 
 * </p>
 * 
 */
public final class EDRNDateUtils {

    /* our log stream */
    private static Logger LOG = Logger.getLogger(EDRNDateUtils.class.getName());

    /* formatter for EDRN style production date times */
    private static SimpleDateFormat UtcDateFormatter_ProdDateTime = new SimpleDateFormat(
            "yyMMddHHmmss");

    /* formatter for EDRN style production dates */
    private static SimpleDateFormat UtcDateFormatter_ProdDate = new SimpleDateFormat(
            "yyMMdd");

    static {
        UtcDateFormatter_ProdDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        UtcDateFormatter_ProdDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private EDRNDateUtils() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }

    public static SimpleDateFormat getProdDateTimeUtcFormatter() {
        return UtcDateFormatter_ProdDateTime;
    }

    public static SimpleDateFormat getProdDateUtcFormatter() {
        return UtcDateFormatter_ProdDate;
    }

    public static String getProdDateFromProductReceivedTimeString(
            String productReceivedTime) throws InvalidDateException {
        String prodDate = null;

        Date prodDateTimeDate = null;

        try {
            prodDateTimeDate = parseISO8601(productReceivedTime);
        } catch (InvalidDateException e) {
            LOG.log(Level.SEVERE, "Error parsing date time from string: ["
                    + productReceivedTime + "]: Message: " + e.getMessage());
            throw e;
        }

        prodDate = EDRNDateUtils.getProdDateUtcFormatter().format(
                prodDateTimeDate);

        return prodDate;

    }

    public static String getProdDateFromProdDateTimeString(
            String productionDateTime) throws ParseException {
        return getProdDateFromFormattingAndOrigString(productionDateTime,
                getProdDateTimeUtcFormatter());
    }

    private static String getProdDateFromFormattingAndOrigString(
            String origStr, SimpleDateFormat formatter) throws ParseException {
        String prodDate = null;

        Date prodDateTimeDate = null;

        try {
            prodDateTimeDate = formatter.parse(origStr);
        } catch (ParseException e) {
            LOG.log(Level.SEVERE, "Error parsing date time from string: ["
                    + origStr + "]: Message: " + e.getMessage());
            throw e;
        }

        prodDate = EDRNDateUtils.getProdDateUtcFormatter().format(
                prodDateTimeDate);

        return prodDate;
    }

    /**
     * Parse the given string in ISO 8601 format and build a Date object.
     * 
     * @param isodate
     *            the date in ISO 8601 format
     * @return a Date instance
     * @exception InvalidDateException
     *                if the date is not valid
     */
    public static Date parseISO8601(String isodate) throws InvalidDateException {
        Calendar calendar = getCalendar(isodate);
        return calendar.getTime();
    }

    private static String twoDigit(int i) {
        if (i >= 0 && i < 10) {
            return "0" + String.valueOf(i);
        }
        return String.valueOf(i);
    }

    private static String threeDigit(int i) {
        if (i >= 0 && i < 100) {
            return "00" + String.valueOf(i);
        }
        return String.valueOf(i);
    }

    /**
     * Generate a ISO 8601 date
     * 
     * @param date
     *            a Date instance
     * @return a string representing the date in the ISO 8601 format
     */
    public static String getIsoDate(Date date) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
        buffer.append("T");
        buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
        buffer.append(".");
        buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
        buffer.append("Z");
        return buffer.toString();
    }

    public static String getIsoDateThreeDigitMils(Date date) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
        buffer.append("T");
        buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
        buffer.append(".");
        buffer.append(threeDigit(calendar.get(Calendar.MILLISECOND) / 10));
        buffer.append("Z");
        return buffer.toString();
    }

    /**
     * Generate a ISO 8601 date
     * 
     * @param date
     *            a Date instance
     * @return a string representing the date in the ISO 8601 format
     */
    public static String getIsoDateNoMillis(Date date) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
        buffer.append("T");
        buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
        buffer.append("Z");
        return buffer.toString();
    }

    public static Date safeGetDateFromIsoDateTimeString(String isoDateTime) {
        try {
            return DateConvert.isoParse(isoDateTime);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to parse packet date time: ["
                    + isoDateTime + "]");
            return null;
        }
    }

    private static boolean check(StringTokenizer st, String token)
            throws InvalidDateException {
        try {
            if (st.nextToken().equals(token)) {
                return true;
            } else {
                throw new InvalidDateException("Missing [" + token + "]");
            }
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    private static Calendar getCalendar(String isodate)
            throws InvalidDateException {
        // YYYY-MM-DDThh:mm:ss.sTZD
        StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        try {
            // Year
            if (st.hasMoreTokens()) {
                int year = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.YEAR, year);
            } else {
                return calendar;
            }
            // Month
            if (check(st, "-") && (st.hasMoreTokens())) {
                int month = Integer.parseInt(st.nextToken()) - 1;
                calendar.set(Calendar.MONTH, month);
            } else {
                return calendar;
            }
            // Day
            if (check(st, "-") && (st.hasMoreTokens())) {
                int day = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.DAY_OF_MONTH, day);
            } else {
                return calendar;
            }
            // Hour
            if (check(st, "T") && (st.hasMoreTokens())) {
                int hour = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }
            // Minutes
            if (check(st, ":") && (st.hasMoreTokens())) {
                int minutes = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.MINUTE, minutes);
            } else {
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }

            //
            // Not mandatory now
            //

            // Secondes
            if (!st.hasMoreTokens()) {
                return calendar;
            }
            String tok = st.nextToken();
            if (tok.equals(":")) { // secondes
                if (st.hasMoreTokens()) {
                    int secondes = Integer.parseInt(st.nextToken());
                    calendar.set(Calendar.SECOND, secondes);
                    if (!st.hasMoreTokens()) {
                        return calendar;
                    }
                    // frac sec
                    tok = st.nextToken();
                    if (tok.equals(".")) {
                        // bug fixed, thx to Martin Bottcher
                        String nt = st.nextToken();
                        while (nt.length() < 3) {
                            nt += "0";
                        }
                        nt = nt.substring(0, 3); // Cut trailing chars..
                        int millisec = Integer.parseInt(nt);
                        // int millisec = Integer.parseInt(st.nextToken()) * 10;
                        calendar.set(Calendar.MILLISECOND, millisec);
                        if (!st.hasMoreTokens()) {
                            return calendar;
                        }
                        tok = st.nextToken();
                    } else {
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                } else {
                    throw new InvalidDateException("No secondes specified");
                }
            } else {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
            // Timezone
            if (!tok.equals("Z")) { // UTC
                if (!(tok.equals("+") || tok.equals("-"))) {
                    throw new InvalidDateException("only Z, + or - allowed");
                }
                boolean plus = tok.equals("+");
                if (!st.hasMoreTokens()) {
                    throw new InvalidDateException("Missing hour field");
                }
                int tzhour = Integer.parseInt(st.nextToken());
                int tzmin = 0;
                if (check(st, ":") && (st.hasMoreTokens())) {
                    tzmin = Integer.parseInt(st.nextToken());
                } else {
                    throw new InvalidDateException("Missing minute field");
                }
                if (plus) {
                    calendar.add(Calendar.HOUR, -tzhour);
                    calendar.add(Calendar.MINUTE, -tzmin);
                } else {
                    calendar.add(Calendar.HOUR, tzhour);
                    calendar.add(Calendar.MINUTE, tzmin);
                }
            }
        } catch (NumberFormatException ex) {
            throw new InvalidDateException("[" + ex.getMessage()
                    + "] is not an integer");
        }
        return calendar;
    }

}
