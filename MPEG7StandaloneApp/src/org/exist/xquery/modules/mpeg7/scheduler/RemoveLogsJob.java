package org.exist.xquery.modules.mpeg7.scheduler;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.exist.scheduler.JobException;
import org.exist.scheduler.UserJavaJob;
import org.exist.storage.BrokerPool;

/**
 * Add the following XML fragment to $EXIST_HOME conf.xml to enable this scheduled task
 *     <job type="user" name="removeLogs" 
            class="org.exist.xquery.modules.mpeg7.scheduler.RemoveLogsJob"
            cron-trigger="0 0 0 ? * SUN *"> <!--every Sunday at 00:00 - http://www.cronmaker.com/-->
        </job>
 * This class adds a scheduled job to delete the past app log files 
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class RemoveLogsJob extends UserJavaJob {

    private String jobName = this.getClass().getName();
    private static final Logger logger = Logger.getLogger(RemoveLogsJob.class);

    @Override
    public void execute(BrokerPool bp, Map<String, ?> map) throws JobException {
        try {
            String basePath = "./../logs/annotation/";
            File fileDir = new File(basePath);
            if (fileDir.isDirectory()) {
                List listFile = Arrays.asList(fileDir.list());
                Collections.sort(listFile, Collections.reverseOrder());
                int index = 0;
                for (Iterator it = listFile.iterator(); it.hasNext();) {
                    index++;
                    Object s = it.next();
                    if (index > 1) {
                        File f = new File(fileDir.getPath() + "/" + s);
                        boolean isDeleted = f.delete();                     
                    }

                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public String getName() {
        return jobName;
    }

    @Override
    public void setName(String name) {
        this.jobName = name;
    }

}
