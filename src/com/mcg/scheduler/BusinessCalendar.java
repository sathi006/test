/**
 *
 */
package com.mcg.scheduler;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.quartz.Calendar;
import org.quartz.impl.calendar.HolidayCalendar;

/**
 * The Class BusinessCalendar.
 *
 * @author ANAB
 */
public class BusinessCalendar extends HolidayCalendar implements Serializable,
        Calendar {

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = -2218261784344191146L;

        /** Constant FIVE. */
        private static final int FIVE = 5;

        /** Constant ONE. */
        private static final int ONE = 1;

        /** dates. */
        private TreeSet<Date> dates = new TreeSet<Date>();

        /**
         * Instantiates a new business calendar.
         */
        public BusinessCalendar() {
        }

        /**
         * Instantiates a new business calendar.
         *
         * @param baseCalendar the base calendar
         */
        public BusinessCalendar(final org.quartz.Calendar baseCalendar) {
          super(baseCalendar);
        }

        /**
         * Instantiates a new business calendar.
         *
         * @param timeZone the time zone
         */
        public BusinessCalendar(final TimeZone timeZone) {
          super(timeZone);
        }

        /**
         * Instantiates a new business calendar.
         *
         * @param baseCalendar the base calendar
         * @param timeZone the time zone
         */
        public BusinessCalendar(final org.quartz.Calendar baseCalendar,
            final TimeZone timeZone) {
          super(baseCalendar, timeZone);
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.
         * HolidayCalendar#isTimeIncluded(long)
         */
        /**
         * Checks if is time included.
         *
         * @param timeStamp long
         * @return timeIncluded boolean
         */
        public final boolean isTimeIncluded(final long timeStamp) {
          if (!super.isTimeIncluded(timeStamp)) {
            return false;
          }

          Date lookFor = getStartOfDayJavaCalendar(timeStamp).getTime();

          return !this.dates.contains(lookFor);
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.
         * HolidayCalendar#getNextIncludedTime(long)
         */
        /**
         * Gets the next included time.
         *
         * @param timeStamp long
         * @return nextIncludedTime long
         */
        public final long getNextIncludedTime(final long timeStamp) {
          long baseTime = super.getNextIncludedTime(timeStamp);
          java.util.Calendar day = null;
          if (baseTime > 0L && baseTime > timeStamp) {
              day = getStartOfDayJavaCalendar(baseTime);
              while (!isTimeIncluded(day.getTime().getTime())) {
                day.add(FIVE, ONE);
              }
          }

          return day.getTime().getTime();
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.HolidayCalendar
         * #addExcludedDate(java.util.Date)
         */
        /**
         * Adds the excluded date.
         *
         * @param excludedDate1 Date
         */
        public final void addExcludedDate(final Date excludedDate1) {
          Date date = getStartOfDayJavaCalendar(
              excludedDate1.getTime()).getTime();
          this.dates.add(date);
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.HolidayCalendar
         * #removeExcludedDate(java.util.Date)
         */
        /**
         * Removes the excluded date.
         *
         * @param dateToRemove Date
         */
        public final void removeExcludedDate(
            final Date dateToRemove) {
          Date date = getStartOfDayJavaCalendar(
              dateToRemove.getTime()).getTime();
          this.dates.remove(date);
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.BaseCalendar#createJavaCalendar(long)
         */
        @Override
    protected final java.util.Calendar createJavaCalendar(
                final long timeStamp) {
                java.util.Calendar calendar = createJavaCalendar();
                calendar.setTime(new Date(timeStamp));
                return calendar;
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.BaseCalendar
         * #getStartOfDayJavaCalendar(long)
         */
        @Override
    protected final java.util.Calendar getStartOfDayJavaCalendar(
                final long timeInMillis) {
                java.util.Calendar startOfDay =
                        createJavaCalendar(timeInMillis);
                return startOfDay;
        }

        /* (non-Javadoc)
         * @see org.quartz.impl.calendar.HolidayCalendar#getExcludedDates()
         */
        /**
         * Gets the excluded dates.
         *
         * @return excludedDates SortedSet<Date>
         */
        public final SortedSet<Date> getExcludedDates() {
          return Collections.unmodifiableSortedSet(this.dates);
        }
}
