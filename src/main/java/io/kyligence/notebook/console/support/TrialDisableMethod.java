package io.kyligence.notebook.console.support;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.exception.MethodDisabledException;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TrialDisableMethod {
    private static final NotebookConfig config = NotebookConfig.getInstance();

    @Before("@annotation(io.kyligence.notebook.console.support.DisableInTrial)")
    public void disable() throws MethodDisabledException {
        if (config.getIsTrial()) throw new MethodDisabledException();
    }
}
