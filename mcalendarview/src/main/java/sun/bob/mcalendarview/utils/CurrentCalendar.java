package sun.bob.mcalendarview.utils;

import java.util.Calendar;
import java.util.Locale;

import sun.bob.mcalendarview.vo.DateData;

/**
 * Created by bob.sun on 15/8/27.
 */
public class CurrentCalendar {
    public static DateData getCurrentDateData(){
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        return new DateData(calendar.get(calendar.YEAR), calendar.get(calendar.MONTH) + 2, calendar.get(calendar.DAY_OF_MONTH));
    }

}
