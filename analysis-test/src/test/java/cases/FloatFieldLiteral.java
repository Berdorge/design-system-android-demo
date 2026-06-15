package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.DesignAnalysisTracker;

public class FloatFieldLiteral {
    private float field;

    public void test(Context context) {
        field = 42.0F;
        returnField();
        DesignAnalysisMark returnValueMark = DesignAnalysisTracker.getReturnValue();
        assertThat(returnValueMark).isNotNull();
        assertThat(returnValueMark.getDescription()).startsWith("literal");
    }

    private float returnField() {
        return field;
    }
}
