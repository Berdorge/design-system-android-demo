package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.DesignAnalysisTracker;

public class FloatReturnLiteral {
    public void test(Context context) {
        returnLiteral();
        DesignAnalysisMark returnValueMark = DesignAnalysisTracker.getReturnValue();
        assertThat(returnValueMark).isNotNull();
        assertThat(returnValueMark.getDescription()).startsWith("literal");
    }

    private static float returnLiteral() {
        return 42.0F;
    }
}
