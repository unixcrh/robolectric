package com.xtremelabs.robolectric.shadows;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadows the {@code android.app.AlarmManager} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlarmManager.class)
public class ShadowAlarmManager {

    private List<ScheduledAlarm> scheduledAlarms = new ArrayList<ScheduledAlarm>();

    @Implementation
    public void set(int type, long triggerAtTime, PendingIntent operation) {
        scheduledAlarms.add(new ScheduledAlarm(type, triggerAtTime, operation));
    }

    /**
     * Non-Android accessor consumes and returns the next scheduled alarm on the
     * AlarmManager's stack.
     *
     * @return the next scheduled alarm, wrapped in a
     *         {@link ShadowAlarmManager.ScheduledAlarm} object
     */
    public ScheduledAlarm getNextScheduledAlarm() {
        if (scheduledAlarms.isEmpty()) {
            return null;
        } else {
            return scheduledAlarms.remove(0);
        }
    }

    /**
     * Non-Android accessor returns the most recent scheduled alarm without
     * consuming it.
     *
     * @return the most recently scheduled alarm, wrapped in a
     *         {@link ShadowAlarmManager.ScheduledAlarm} object
     */
    public ScheduledAlarm peekNextScheduledAlarm() {
        if (scheduledAlarms.isEmpty()) {
            return null;
        } else {
            return scheduledAlarms.get(0);
        }
    }

    public List<ScheduledAlarm> getScheduledAlarms() {
        return scheduledAlarms;
    }

    @Implementation
    public void cancel(PendingIntent pendingIntent) {
        final Intent intentTypeToRemove = shadowOf(pendingIntent).getSavedIntent();
        for (ScheduledAlarm scheduledAlarm : new ArrayList<ScheduledAlarm>(scheduledAlarms)) {
            final Intent alarmIntent = shadowOf(scheduledAlarm.operation).getSavedIntent();
            if (shadowOf(intentTypeToRemove).getIntentClass().equals(shadowOf(alarmIntent).getIntentClass())) {
                scheduledAlarms.remove(scheduledAlarm);
            }
        }
    }

    /**
     * Container object to hold an PendingIntent, together with the alarm
     * parameters used in a call to {@code AlarmManager}
     */
    public class ScheduledAlarm {
        public int type;
        public long triggerAtTime;
        public long interval;
        public PendingIntent operation;

        public ScheduledAlarm(int type, long triggerAtTime, PendingIntent operation) {
            this(type, triggerAtTime, 0, operation);
        }

        public ScheduledAlarm(int type, long triggerAtTime, long interval, PendingIntent operation) {
            this.type = type;
            this.triggerAtTime = triggerAtTime;
            this.operation = operation;
            this.interval = interval;
        }
    }
}
