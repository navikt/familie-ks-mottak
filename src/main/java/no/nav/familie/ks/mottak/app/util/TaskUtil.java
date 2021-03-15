package no.nav.familie.ks.mottak.app.util;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class TaskUtil {

    private TaskUtil(){
        //NOP
    }

    public static final LocalDateTime nesteTriggertidEksluderHelg(@NotNull LocalDateTime dt) {
        if (dt.getDayOfWeek() == DayOfWeek.SATURDAY || dt.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return dt.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(7).withMinute(0).withSecond(0);
        } else {
            return dt;
        }
    }

}
