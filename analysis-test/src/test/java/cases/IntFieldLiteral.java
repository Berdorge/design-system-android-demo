package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.DesignAnalysisTracker;

public class IntFieldLiteral {
    private int field;

    public void test(Context context) {
        field = 42;
        returnField();
        DesignAnalysisMark returnValueMark = DesignAnalysisTracker.getReturnValue();
        assertThat(returnValueMark).isNotNull();
        assertThat(returnValueMark.getDescription()).startsWith("literal");
    }

    private int returnField() {
        return field;
    }
}
