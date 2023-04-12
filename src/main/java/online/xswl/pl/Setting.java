package online.xswl.pl;

import online.xswl.pl.utils.ExceptionBarrier;
import online.xswl.pl.utils.ExceptionBarrier.ExceptionLoggingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * configure some policy
 *
 * @author PL
 */
public class Setting {

    private Setting() {}

    protected static final Setting instance = new Setting();

    private final Logger log = LoggerFactory.getLogger(ExceptionBarrier.class);


    private ExceptionLoggingConsumer exceptionLoggingConsumer = e -> log.warn("该异常已被Safer暂时忽略，请及时处理", e);

    public Setting setExceptionLoggingConsumer(ExceptionLoggingConsumer exceptionLoggingConsumer) {
        assert exceptionLoggingConsumer != null;
        this.exceptionLoggingConsumer = exceptionLoggingConsumer;
        return this;
    }

    public ExceptionLoggingConsumer getExceptionLoggingConsumer() {
                                                                return exceptionLoggingConsumer;
                                                                                                }

}
