import com.study.spring.user.UserDaoTest;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.util.Objects;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class UserDaoMainTest {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectPackage("com.study.spring.user"), selectClass(UserDaoTest.class))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        launcher.execute(request, listener);
        TestExecutionSummary summary = listener.getSummary();

        System.out.println("# of containers found: " + summary.getContainersFoundCount());
        System.out.println("# of containers skipped: " + summary.getContainersSkippedCount());
        System.out.println("# of tests found: " + summary.getTestsFoundCount());
        System.out.println("# of tests skipped: " + summary.getTestsSkippedCount());

        summary.printTo(new PrintWriter(System.out));

        if (summary.getFailures().size() > 0) {
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                if (Objects.nonNull(failure.getException())) {
                    throw new RuntimeException(failure.getException());
                }
            }
        }
    }
}
