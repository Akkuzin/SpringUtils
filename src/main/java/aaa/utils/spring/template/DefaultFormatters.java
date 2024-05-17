package aaa.utils.spring.template;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DefaultFormatters {

  public static final String XML_DATE_FORMAT = "yyyy-MM-dd";
  public static final String DEFAULT_TIMESTAMP_FORMAT = "dd.MM.yy HH:mm:ss:SSS";
  public static final String SHORT_DATE_TIME_FORMAT = "dd.MM.yy HH:mm";
  public static final String SHORT_DATE_FORMAT = "dd.MM.yy";
  public static final String TIME_FORMAT = "HH:mm";
  public static final String DEFAULT_DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
  public static final String DEFAULT_DATE_TIME_WITH_SECONDS_FORMAT = "dd.MM.yyyy HH:mm:ss";
  public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";
  public static final String MONTH_DATE_FORMAT = "LLLL yyyy";

  public abstract static class NotSettableThreadLocal<T> extends ThreadLocal<T> {
    public void set(NumberFormat value) {
      throw new UnsupportedOperationException();
    }
  }

  private static final ThreadLocal<NumberFormat> DEFAULT_NUMBER_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeDefaultNumberFormat());
  private static final ThreadLocal<DateFormat> XML_DATE_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeXMLDateTimeFormat());
  private static final ThreadLocal<NumberFormat> NUMBER_STRIPPING_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeNumberStrippingFormat());
  private static final ThreadLocal<NumberFormat> NUMBER_NON_STRIPPING_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeNumberNonStrippingFormat());
  private static final ThreadLocal<NumberFormat> NATURAL_NUMBER_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeNaturalNumberFormat());

  public static NumberFormat makeNaturalNumberFormat() {
    return makeDefaultNumberFormat(0);
  }

  public static NumberFormat makeDefaultNumberFormat() {
    return makeDefaultNumberFormat(2);
  }

  public static NumberFormat makeDefaultNumberFormat(int digitsAfterComma) {
    NumberFormat result = new DecimalFormat("###,###.##", makeDecimalFormatSymbols());
    result.setMinimumFractionDigits(digitsAfterComma);
    result.setMaximumFractionDigits(digitsAfterComma);
    result.setGroupingUsed(true);
    result.setRoundingMode(RoundingMode.HALF_UP);
    return result;
  }

  public static NumberFormat makeNoGroupingNumberFormat(int digitsAfterComma) {
    NumberFormat result = makeDefaultNumberFormat(digitsAfterComma);
    result.setGroupingUsed(false);
    return result;
  }

  public static NumberFormat makeExcelNumberFormat(int digitsAfterComma) {
    NumberFormat result = new DecimalFormat("###,###.##", makeExcelFormatSymbols());
    result.setMinimumFractionDigits(digitsAfterComma);
    result.setMaximumFractionDigits(digitsAfterComma);
    result.setGroupingUsed(false);
    result.setRoundingMode(RoundingMode.HALF_UP);
    return result;
  }

  public static DecimalFormatSymbols makeExcelFormatSymbols() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setDecimalSeparator(',');
    decimalFormatSymbols.setGroupingSeparator(' ');
    return decimalFormatSymbols;
  }

  public static DecimalFormatSymbols makeDecimalFormatSymbols() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setDecimalSeparator('.');
    decimalFormatSymbols.setGroupingSeparator(' ');
    return decimalFormatSymbols;
  }

  public static NumberFormat getDefaultNumberFormat() {
    return DEFAULT_NUMBER_FORMAT_HOLDER.get();
  }

  public static DateFormat makeDefaultDateFormat() {
    DateFormat result = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    result.setLenient(false);
    return result;
  }

  private static DateTimeFormatter defaultDateFormatter;

  public static DateTimeFormatter getDefaultDateFormatter() {
    if (defaultDateFormatter == null) {
      defaultDateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    }
    return defaultDateFormatter;
  }

  public static DateFormat makeMonthDateFormat(Locale locale) {
    DateFormat result = new SimpleDateFormat(MONTH_DATE_FORMAT, locale);
    result.setLenient(false);
    return result;
  }

  public static DateFormat makeDefaultDateTimeFormat() {
    DateFormat result = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
    result.setLenient(false);
    return result;
  }

  public static DateFormat makeDefaultDateWithSecondsTimeFormat() {
    DateFormat result = new SimpleDateFormat(DEFAULT_DATE_TIME_WITH_SECONDS_FORMAT);
    result.setLenient(false);
    return result;
  }

  private static DateTimeFormatter defaultDateTimeFormatter;

  public static DateTimeFormatter getDefaultDateTimeFormatter() {
    if (defaultDateTimeFormatter == null) {
      defaultDateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    }
    return defaultDateTimeFormatter;
  }

  public static DateFormat makeDefaultShortDateFormat() {
    DateFormat result = new SimpleDateFormat(SHORT_DATE_FORMAT);
    result.setLenient(false);
    return result;
  }

  public static DateFormat makeDefaultTimeFormat() {
    DateFormat result = new SimpleDateFormat(TIME_FORMAT);
    result.setLenient(false);
    return result;
  }

  public static DateFormat makeDefaultShortDateTimeFormat() {
    DateFormat result = new SimpleDateFormat(SHORT_DATE_TIME_FORMAT);
    result.setLenient(false);
    return result;
  }

  public static DateFormat makeDefaultTimestampFormat() {
    DateFormat result = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    result.setLenient(false);
    return result;
  }

  public static DateFormat getXMLDateFormat() {
    return XML_DATE_FORMAT_HOLDER.get();
  }

  public static DateFormat makeXMLDateTimeFormat() {
    DateFormat result = new SimpleDateFormat(XML_DATE_FORMAT);
    result.setLenient(false);
    return result;
  }

  public static NumberFormat makeNumberStrippingFormat() {
    NumberFormat result = makeNoGroupingNumberFormat(2);
    result.setMinimumFractionDigits(0);
    return result;
  }

  public static NumberFormat getNumberStrippingFormat() {
    return NUMBER_STRIPPING_FORMAT_HOLDER.get();
  }

  public static NumberFormat makeNumberNonStrippingFormat() {
    return makeNoGroupingNumberFormat(2);
  }

  public static NumberFormat getNumberNonStrippingFormat() {
    return NUMBER_NON_STRIPPING_FORMAT_HOLDER.get();
  }

  public static NumberFormat getNaturalNumberFormat() {
    return NATURAL_NUMBER_FORMAT_HOLDER.get();
  }
}
