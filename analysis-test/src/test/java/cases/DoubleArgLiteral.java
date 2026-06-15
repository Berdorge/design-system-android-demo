package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.DesignAnalysisTracker;

public class DoubleArgLiteral {
    public void test(Context context) {
        returnArg(42.0);
        DesignAnalysisMark returnValueMark = DesignAnalysisTracker.getReturnValue();
        assertThat(returnValueMark).isNotNull();
        assertThat(returnValueMark.getDescription()).startsWith("literal");
    }

    private static double returnArg(double a) {
        return a;
    }
}
