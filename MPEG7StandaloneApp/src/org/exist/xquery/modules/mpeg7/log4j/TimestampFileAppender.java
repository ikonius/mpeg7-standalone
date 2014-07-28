package org.exist.xquery.modules.mpeg7.log4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.log4j.FileAppender;


/**
 * TimestampFileAppender is a log4j appender that creates a timestamped log
 * file.
 *
 * @author Viktor Bresan 
 */
public class TimestampFileAppender extends FileAppender {

    private static final String TARGET = "\\{timestamp\\}";

    protected String timestampPattern = null;

    @Override
    public void setFile(String file) {

        if (timestampPattern != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(timestampPattern);
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
            super.setFile(file.replaceAll(TARGET, sdf.format(Calendar.getInstance().getTime())));
        } else {
            super.setFile(file);
        }
    }

    /**
     *
     * @param fileName
     * @param append
     * @param bufferedIO
     * @param bufferSize
     * @throws java.io.IOException
     */
    @Override
    public void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {

        if (timestampPattern != null) {
             SimpleDateFormat sdf = new SimpleDateFormat(timestampPattern);
              sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
            super.setFile(fileName.replaceAll(TARGET, sdf.format(Calendar.getInstance().getTime())), append, bufferedIO, bufferSize);
        } else {
            super.setFile(fileName, append, bufferedIO, bufferSize);
        }
    }

    /**
     *
     * @return
     */
    public String getTimestampPattern() {
        return timestampPattern;
    }

    /**
     *
     * @param timestampPattern
     */
    public void setTimestampPattern(String timestampPattern) {
        this.timestampPattern = timestampPattern;
    }
}
