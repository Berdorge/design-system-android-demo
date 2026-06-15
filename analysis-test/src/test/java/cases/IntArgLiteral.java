package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.DesignAnalysisTracker;

public class IntArgLiteral {
    public void test(Context context) {
        returnArg(42);
        DesignAnalysisMark returnValueMark = DesignAnalysisTracker.getReturnValue();
        assertThat(returnValueMark).isNotNull();
        assertThat(returnValueMark.getDescription()).startsWith("literal");
    }

    private static int returnArg(int a) {
        return a;
    }
}
