package aaa.utils.spring.template;

import java.math.RoundingMode;
import java.text.*;
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

  private static final ThreadLocal<NumberFormat> DEFAULT_DOUBLE_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeDefaultDoubleFormat());
  private static final ThreadLocal<DateFormat> XML_DATE_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeXMLDateTimeFormat());
  private static final ThreadLocal<NumberFormat> DOUBLE_STRIPPING_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeDoubleStrippingFormat());
  private static final ThreadLocal<NumberFormat> DOUBLE_NON_STRIPPING_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeDoubleNonStrippingFormat());
  private static final ThreadLocal<NumberFormat> NATURAL_NUMBER_FORMAT_HOLDER =
      NotSettableThreadLocal.withInitial(() -> makeNaturalNumberFormat());

  public static NumberFormat makeNaturalNumberFormat() {
    return makeDefaultDoubleFormat(0);
  }

  public static NumberFormat makeDefaultDoubleFormat() {
    return makeDefaultDoubleFormat(2);
  }

  public static NumberFormat makeDefaultDoubleFormat(int digitsAfterComma) {
    NumberFormat result = new DecimalFormat("###,###.##", makeDecimalFormatSymbols());
    result.setMinimumFractionDigits(digitsAfterComma);
    result.setMaximumFractionDigits(digitsAfterComma);
    result.setGroupingUsed(true);
    result.setRoundingMode(RoundingMode.HALF_UP);
    return result;
  }

  public static NumberFormat makeNoGroupingDoubleFormat(int digitsAfterComma) {
    NumberFormat result = makeDefaultDoubleFormat(digitsAfterComma);
    result.setGroupingUsed(false);
    return result;
  }

  public static NumberFormat makeExcelDoubleFormat(int digitsAfterComma) {
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

  public static NumberFormat getDefaultDoubleFormat() {
    return DEFAULT_DOUBLE_FORMAT_HOLDER.get();
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

  public static NumberFormat makeDoubleStrippingFormat() {
    NumberFormat result = makeNoGroupingDoubleFormat(2);
    result.setMinimumFractionDigits(0);
    return result;
  }

  public static NumberFormat getDoubleStrippingFormat() {
    return DOUBLE_STRIPPING_FORMAT_HOLDER.get();
  }

  public static NumberFormat makeDoubleNonStrippingFormat() {
    return makeNoGroupingDoubleFormat(2);
  }

  public static NumberFormat getDoubleNonStrippingFormat() {
    return DOUBLE_NON_STRIPPING_FORMAT_HOLDER.get();
  }

  public static NumberFormat getNaturalNumberFormat() {
    return NATURAL_NUMBER_FORMAT_HOLDER.get();
  }
}
